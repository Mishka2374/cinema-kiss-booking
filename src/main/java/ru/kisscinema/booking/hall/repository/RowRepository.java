package ru.kisscinema.booking.hall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kisscinema.booking.hall.model.Row;
import java.util.List;

public interface RowRepository extends JpaRepository<Row, Long> {
    List<Row> findByHallIdOrderByRowNumberAsc(Long hallId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.seat.row.id = :rowId")
    boolean existsBookingsByRowId(@Param("rowId") Long rowId);
}