package ru.kisscinema.booking.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.kisscinema.booking.audit.service.AuditService;
import ru.kisscinema.booking.booking.repository.BookingRepository;
import ru.kisscinema.booking.hall.model.Hall;
import ru.kisscinema.booking.hall.repository.HallRepository;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.movie.repository.MovieRepository;
import ru.kisscinema.booking.session.dto.SessionDto;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.session.service.SessionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private HallRepository hallRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AuditService auditService;

    @InjectMocks private SessionService sessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllSessions_whenMovieIdNull_shouldReturnAllSessions() {
        Session s = new Session();
        when(sessionRepository.findAll()).thenReturn(List.of(s));

        List<Session> result = sessionService.getAllSessions(null);

        assertEquals(1, result.size());
        verify(sessionRepository, times(1)).findAll();
    }

    @Test
    void getAllSessions_whenMovieIdNotNull_shouldReturnFilteredSessions() {
        Session s = new Session();
        when(sessionRepository.findByMovieId(1L)).thenReturn(List.of(s));

        List<Session> result = sessionService.getAllSessions(1L);

        assertEquals(1, result.size());
        verify(sessionRepository, times(1)).findByMovieId(1L);
    }

    @Test
    void getSessionById_shouldReturnSession() {
        Session s = new Session();
        s.setId(1L);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(s));

        Session result = sessionService.getSessionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createSession_shouldSaveAndReturnSession() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setDurationMinutes(120);

        Hall hall = new Hall();
        hall.setId(1L);

        SessionDto dto = new SessionDto(1L, 1L, 1L, LocalDateTime.of(2025,12,1,12,0), new BigDecimal(500));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(sessionRepository.existsByHallIdAndStartTimeLessThanAndEndTimeGreaterThan(anyLong(), any(), any())).thenReturn(false);

        Session savedSession = new Session();
        savedSession.setId(1L);
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

        Session result = sessionService.createSession(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(auditService, times(1)).log(eq("Session"), eq(1L), eq("CREATE"), any(), contains("Создан сеанс"));
    }

    @Test
    void getSessionsByDate_shouldReturnSessionsInRange() {
        LocalDate date = LocalDate.of(2025,12,1);
        Session s = new Session();
        when(sessionRepository.findByStartTimeBetweenOrderByStartTimeAsc(any(), any())).thenReturn(List.of(s));

        List<Session> result = sessionService.getSessionsByDate(date);

        assertEquals(1, result.size());
        verify(sessionRepository, times(1)).findByStartTimeBetweenOrderByStartTimeAsc(any(), any());
    }

    @Test
    void deleteSession_shouldCallRepositoryAndAudit() {
        when(bookingRepository.existsBySessionId(1L)).thenReturn(false);
        doNothing().when(sessionRepository).deleteById(1L);

        sessionService.deleteSession(1L);

        verify(sessionRepository, times(1)).deleteById(1L);
        verify(auditService, times(1)).log(eq("Session"), eq(1L), eq("DELETE"), any(), contains("Сеанс удалён"));
    }

    @Test
    void deleteSession_whenBookingsExist_shouldThrow() {
        when(bookingRepository.existsBySessionId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> sessionService.deleteSession(1L));
        assertEquals("Невозможно удалить сеанс: существуют брони на этот сеанс", ex.getMessage());
    }
}

