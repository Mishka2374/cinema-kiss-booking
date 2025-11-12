package ru.kisscinema.booking.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.session.model.Session;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByMovieId(Long movieId);
    boolean existsByHallIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long hallId, LocalDateTime endTime, LocalDateTime startTime);
    List<Session> findByStartTimeBetweenOrderByStartTimeAsc(
            LocalDateTime start, LocalDateTime end
    );
    boolean existsByHallId(Long hallId);
    boolean existsByMovieId(Long movieId);
}