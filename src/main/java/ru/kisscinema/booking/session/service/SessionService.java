package ru.kisscinema.booking.session.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.booking.repository.BookingRepository;
import ru.kisscinema.booking.movie.repository.MovieRepository;
import ru.kisscinema.booking.hall.repository.HallRepository;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.session.dto.SessionDto;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.hall.model.Hall;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<Session> getAllSessions(Long movieId) {
        return movieId != null ? sessionRepository.findByMovieId(movieId) : sessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    @Transactional
    public Session createSession(SessionDto dto) {
        Movie movie = movieRepository.findById(dto.movieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Hall hall = hallRepository.findById(dto.hallId())
                .orElseThrow(() -> new RuntimeException("Hall not found"));

        LocalDateTime start = dto.startTime();
        LocalDateTime end = start.plusMinutes(movie.getDurationMinutes());

        if (sessionRepository.existsByHallIdAndStartTimeLessThanAndEndTimeGreaterThan(
                hall.getId(), end, start)) {
            throw new RuntimeException("Сеанс пересекается с другим в этом зале");
        }

        Session session = new Session();
        session.setMovie(movie);
        session.setHall(hall);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setPrice(dto.price());
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<Session> getSessionsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return sessionRepository.findByStartTimeBetweenOrderByStartTimeAsc(start, end);
    }

    @Transactional
    public void deleteSession(Long id) {
        if (bookingRepository.existsBySessionId(id)) {
            throw new RuntimeException("Невозможно удалить сеанс: существуют брони на этот сеанс");
        }
        sessionRepository.deleteById(id);
    }
}