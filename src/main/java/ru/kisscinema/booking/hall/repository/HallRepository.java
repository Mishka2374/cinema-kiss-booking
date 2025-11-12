package ru.kisscinema.booking.hall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.hall.model.Hall;

public interface HallRepository extends JpaRepository<Hall, Long> {}