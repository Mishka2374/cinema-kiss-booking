package ru.kisscinema.booking.hall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.hall.model.Seat;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRowIdOrderBySeatNumberAsc(Long rowId);
    List<Seat> findByRowHallId(Long hallId);
    Optional<Seat> findByRowHallIdAndRowRowNumberAndSeatNumber(
            Long hallId,
            Integer rowNumber,
            Integer seatNumber
    );

}