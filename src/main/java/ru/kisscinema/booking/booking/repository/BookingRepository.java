package ru.kisscinema.booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.booking.model.Booking;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBySessionId(Long sessionId);

    Optional<Booking> findByBookingCode(String code);
    Optional<Booking> findBySessionIdAndSeatId(Long sessionId, Long seatId);
    Optional<Booking> findBySessionIdAndSeatIdAndUserTelegramId(Long sessionId, Long seatId, Long userTelegramId);
}