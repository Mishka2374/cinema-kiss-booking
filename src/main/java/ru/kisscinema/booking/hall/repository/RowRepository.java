package ru.kisscinema.booking.hall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.hall.model.Row;
import java.util.List;

public interface RowRepository extends JpaRepository<Row, Long> {
    List<Row> findByHallIdOrderByRowNumberAsc(Long hallId);
}