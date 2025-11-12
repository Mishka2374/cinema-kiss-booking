package ru.kisscinema.booking.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.movie.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {}
