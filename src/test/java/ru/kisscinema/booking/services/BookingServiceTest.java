package ru.kisscinema.booking.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kisscinema.booking.audit.service.AuditService;
import ru.kisscinema.booking.audit.util.AuditAuthor;
import ru.kisscinema.booking.booking.dto.BookingRequestDto;
import ru.kisscinema.booking.booking.model.Booking;
import ru.kisscinema.booking.booking.model.BookingStatus;
import ru.kisscinema.booking.booking.repository.BookingRepository;
import ru.kisscinema.booking.booking.service.BookingService;
import ru.kisscinema.booking.hall.model.Seat;
import ru.kisscinema.booking.hall.model.Row;
import ru.kisscinema.booking.hall.model.Hall;
import ru.kisscinema.booking.hall.repository.SeatRepository;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private AuditService auditService;

    @InjectMocks private BookingService bookingService;

    private Session session;
    private Seat seat;

    @BeforeEach
    void setUp() {
        // prepare a Session
        session = new Session();
        session.setId(100L);
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        session.setMovie(movie);
        session.setStartTime(LocalDateTime.now());
        session.setPrice(BigDecimal.valueOf(500));
        Hall hall = new Hall();
        hall.setId(1L);
        session.setHall(hall);

        // prepare a Seat
        seat = new Seat();
        seat.setId(10L);
        Row row = new Row();
        row.setRowNumber(5);
        row.setHall(hall);
        seat.setRow(row);
        seat.setSeatNumber(3);
    }

    @Test
    void createBooking_whenNoExistingBooking_shouldCreate() {
        BookingRequestDto dto = new BookingRequestDto(session.getId(), seat.getId());
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
        when(bookingRepository.findBySessionIdAndSeatId(session.getId(), seat.getId()))
                .thenReturn(Optional.empty());


        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking b = invocation.getArgument(0);
                    b.setId(500L);
                    return b;
                });

        var response = bookingService.createBooking(dto, 12345L);

        assertNotNull(response);
        assertEquals("Test Movie", response.movieTitle());
        assertEquals(5, response.rowNumber());
        assertEquals(3, response.seatNumber());
        assertNotNull(response.bookingCode());

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(auditService).log(eq("Booking"), eq(500L), eq("CREATE"), eq(AuditAuthor.USER), contains("Создана бронь"));
    }

    @Test
    void createBooking_whenExistingCancelledBooking_shouldRestore() {
        Booking existing = new Booking();
        existing.setId(777L);
        existing.setStatus(BookingStatus.CANCELLED);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
        when(bookingRepository.findBySessionIdAndSeatId(session.getId(), seat.getId()))
                .thenReturn(Optional.of(existing));

        when(bookingRepository.save(existing)).thenReturn(existing);

        var response = bookingService.createBooking(new BookingRequestDto(session.getId(), seat.getId()), null);

        assertNotNull(response);
        assertEquals("Test Movie", response.movieTitle());
        assertEquals(5, response.rowNumber());
        assertEquals(3, response.seatNumber());
        assertNotNull(response.bookingCode());

        verify(bookingRepository, times(1)).save(existing);
        verify(auditService).log(eq("Booking"), eq(777L), eq("UPDATE"), eq("SYSTEM"), contains("Бронь восстановлена"));
    }

    @Test
    void createBooking_whenAlreadyReserved_shouldThrow() {
        Booking existing = new Booking();
        existing.setStatus(BookingStatus.RESERVED);

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
        when(bookingRepository.findBySessionIdAndSeatId(session.getId(), seat.getId()))
                .thenReturn(Optional.of(existing));

        BookingRequestDto dto = new BookingRequestDto(session.getId(), seat.getId());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(dto, 123L));
        assertEquals("Место уже забронировано", ex.getMessage());
    }

    @Test
    void useBooking_whenExists_shouldMarkUsed() {
        String code = "ABC123";
        Booking booking = new Booking();
        booking.setBookingCode(code);
        booking.setStatus(BookingStatus.RESERVED);
        when(bookingRepository.findByBookingCode(code)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.useBooking(code);

        assertEquals(BookingStatus.USED, booking.getStatus());
        verify(auditService).log(eq("Booking"), isNull(), eq("UPDATE"), eq(AuditAuthor.USER), contains("подтверждена"));
    }

    @Test
    void cancelBookingByUser_valid_shouldCancel() {
        Long sessId = session.getId();
        int row = seat.getRow().getRowNumber();
        int num = seat.getSeatNumber();
        Long chatId = 222L;

        // Seat lookup
        when(sessionRepository.findById(sessId)).thenReturn(Optional.of(session));
        when(seatRepository.findByRowHallIdAndRowRowNumberAndSeatNumber(1L, row, num))
                .thenReturn(Optional.of(seat));

        Booking booking = new Booking();
        booking.setId(999L);
        booking.setStatus(BookingStatus.RESERVED);
        booking.setUserTelegramId(chatId);

        when(bookingRepository.findBySessionIdAndSeatIdAndUserTelegramId(sessId, seat.getId(), chatId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBookingByUser(sessId, row, num, chatId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(auditService).log(eq("Booking"), eq(999L), eq("UPDATE"), eq(AuditAuthor.USER), contains("отменена пользователем"));
    }
}
