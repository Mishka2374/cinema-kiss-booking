package ru.kisscinema.booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kisscinema.booking.booking.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBySessionIdAndSeatId(Long sessionId, Long seatId);
    Optional<Booking> findByBookingCode(String bookingCode);
    boolean existsBySessionId(Long sessionId);

    @Query("SELECT b.seat.id FROM Booking b WHERE b.session.id = :sessionId AND b.status = 'RESERVED'")
    List<Long> findReservedSeatIdsBySessionId(@Param("sessionId") Long sessionId);
}