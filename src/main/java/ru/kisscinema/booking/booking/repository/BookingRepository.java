package ru.kisscinema.booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.booking.model.Booking;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByChatId(Long chatId);
    boolean existsBySessionIdAndSeatId(Long sessionId, Long seatId);
}