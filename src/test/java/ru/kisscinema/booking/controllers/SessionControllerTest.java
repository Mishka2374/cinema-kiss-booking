package ru.kisscinema.booking.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.kisscinema.booking.session.controller.SessionController;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.dto.SessionDto;
import ru.kisscinema.booking.session.service.SessionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private SessionController sessionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_shouldReturnSessions() {
        Session s1 = new Session();
        s1.setId(1L);
        Session s2 = new Session();
        s2.setId(2L);

        when(sessionService.getAllSessions(null)).thenReturn(List.of(s1, s2));

        List<Session> result = sessionController.getAll(null);

        assertEquals(2, result.size());
        verify(sessionService, times(1)).getAllSessions(null);
    }

    @Test
    void get_shouldReturnSessionById() {
        Session s = new Session();
        s.setId(1L);

        when(sessionService.getSessionById(1L)).thenReturn(s);

        Session result = sessionController.get(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sessionService, times(1)).getSessionById(1L);
    }

    @Test
    void create_shouldReturnCreatedSession() {
        SessionDto dto = new SessionDto(1L, 1L, 1L, LocalDateTime.now(), new BigDecimal(100));
        Session saved = new Session();
        saved.setId(1L);

        when(sessionService.createSession(dto)).thenReturn(saved);

        Session result = sessionController.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sessionService, times(1)).createSession(dto);
    }

    @Test
    void getSessionsByDate_shouldReturnSessionsForDate() {
        LocalDate date = LocalDate.now();
        Session s = new Session();
        s.setId(1L);

        when(sessionService.getSessionsByDate(date)).thenReturn(List.of(s));

        List<Session> result = sessionController.getSessionsByDate(date);

        assertEquals(1, result.size());
        verify(sessionService, times(1)).getSessionsByDate(date);
    }

    @Test
    void deleteSession_shouldCallService() {
        doNothing().when(sessionService).deleteSession(1L);

        sessionController.deleteSession(1L);

        verify(sessionService, times(1)).deleteSession(1L);
    }
}
