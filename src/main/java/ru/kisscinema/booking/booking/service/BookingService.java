package ru.kisscinema.booking.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.booking.dto.BookingRequestDto;
import ru.kisscinema.booking.booking.dto.BookingResponse;
import ru.kisscinema.booking.booking.model.Booking;
import ru.kisscinema.booking.booking.model.BookingStatus;
import ru.kisscinema.booking.booking.repository.BookingRepository;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.hall.repository.SeatRepository;
import ru.kisscinema.booking.hall.dto.SeatDto;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.model.Seat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequestDto dto) {
        Session session = sessionRepository.findById(dto.sessionId())
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));
        Seat seat = seatRepository.findById(dto.seatId())
                .orElseThrow(() -> new RuntimeException("Место не найдено"));

        if (bookingRepository.existsBySessionIdAndSeatId(dto.sessionId(), dto.seatId())) {
            throw new RuntimeException("Место уже забронировано");
        }

        String code = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking();
        booking.setSession(session);
        booking.setSeat(seat);
        booking.setBookingCode(code);
        booking.setStatus(BookingStatus.RESERVED);
        bookingRepository.save(booking);

        return new BookingResponse(
                code,
                session.getMovie().getTitle(),
                session.getStartTime(),
                session.getPrice(),
                seat.getRow().getRowNumber(),
                seat.getSeatNumber()
        );
    }

    @Transactional(readOnly = true)
    public List<SeatDto> getAvailableSeats(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        List<Long> bookedSeatIds = bookingRepository.findSeatIdsBySessionId(sessionId);
        List<Seat> allSeats = seatRepository.findByRowHallId(session.getHall().getId());

        return allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getId()))
                .map(seat -> new SeatDto(
                        seat.getId(),
                        seat.getRow().getRowNumber(),
                        seat.getSeatNumber()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void useBooking(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена"));
        booking.setStatus(BookingStatus.USED);
        bookingRepository.save(booking);
    }
}