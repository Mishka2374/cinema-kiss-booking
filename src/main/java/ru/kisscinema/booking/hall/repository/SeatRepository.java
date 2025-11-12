package ru.kisscinema.booking.hall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.hall.model.Seat;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRowIdOrderBySeatNumberAsc(Long rowId);
    List<Seat> findByRowHallId(Long hallId);
}