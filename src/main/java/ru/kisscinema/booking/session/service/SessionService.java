package ru.kisscinema.booking.session.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.movie.repository.MovieRepository;
import ru.kisscinema.booking.hall.repository.HallRepository;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.session.dto.SessionDto;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.hall.model.Hall;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepo;
    private final MovieRepository movieRepo;
    private final HallRepository hallRepo;

    @Transactional(readOnly = true)
    public List<Session> getAllSessions(Long movieId) {
        return movieId != null ? sessionRepo.findByMovieId(movieId) : sessionRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Session getSessionById(Long id) {
        return sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    @Transactional
    public Session createSession(SessionDto dto) {
        Movie movie = movieRepo.findById(dto.movieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Hall hall = hallRepo.findById(dto.hallId())
                .orElseThrow(() -> new RuntimeException("Hall not found"));

        LocalDateTime start = dto.startTime();
        LocalDateTime end = start.plusMinutes(movie.getDurationMinutes());

        if (sessionRepo.existsByHallIdAndStartTimeLessThanAndEndTimeGreaterThan(
                hall.getId(), end, start)) {
            throw new RuntimeException("Session overlaps in this hall");
        }

        Session session = new Session();
        session.setMovie(movie);
        session.setHall(hall);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setPrice(dto.price());
        return sessionRepo.save(session);
    }
}