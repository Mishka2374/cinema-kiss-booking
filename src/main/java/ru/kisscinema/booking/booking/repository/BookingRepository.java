package ru.kisscinema.booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.booking.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBySessionIdAndSeatId(Long sessionId, Long seatId);
    List<Long> findSeatIdsBySessionId(Long sessionId);
    Optional<Booking> findByBookingCode(String bookingCode);
    boolean existsBySessionId(Long sessionId);
}