package ru.kisscinema.booking.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.hall.repository.SeatRepository;
import ru.kisscinema.booking.booking.model.Booking;
import ru.kisscinema.booking.booking.model.BookingStatus;
import ru.kisscinema.booking.booking.repository.BookingRepository;
import ru.kisscinema.booking.booking.dto.BookingDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final SessionRepository sessionRepo;
    private final SeatRepository seatRepo;

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings(Long chatId) {
        return chatId != null ? bookingRepo.findByChatId(chatId) : bookingRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Transactional
    public Booking createBooking(BookingDto dto) {
        var session = sessionRepo.findById(dto.sessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
        var seat = seatRepo.findById(dto.seatId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (bookingRepo.existsBySessionIdAndSeatId(dto.sessionId(), dto.seatId())) {
            throw new RuntimeException("Seat already booked");
        }

        Booking booking = new Booking();
        booking.setSession(session);
        booking.setSeat(seat);
        booking.setChatId(dto.chatId());
        return bookingRepo.save(booking);
    }

    @Transactional
    public Booking payBooking(Long id) {
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(BookingStatus.PAID);
        return bookingRepo.save(booking);
    }

    // TODO: реализовать через JPQL
    @Transactional(readOnly = true)
    public List<Long> getAvailableSeats(Long sessionId) {
        return List.of(1L, 2L, 3L);
    }
}