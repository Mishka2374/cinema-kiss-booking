package ru.kisscinema.booking.session.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.session.dto.SessionDto;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.service.SessionService;

import java.time.LocalDate;
import java.util.List;

/**
 * REST контроллер для управления сеансами.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);
    private final SessionService sessionService;

    /**
     * GET /api/sessions
     * Получить список всех сеансов (опционально — по фильму через movieId).
     */
    @GetMapping
    public List<Session> getAll(@RequestParam(required = false) Long movieId) {
        log.info("Получение сеансов (movieId={})...", movieId);
        List<Session> sessions = sessionService.getAllSessions(movieId);
        log.info("Сеансы получены. Количество: {}", sessions.size());
        return sessions;
    }

    /**
     * GET /api/sessions/{id}
     * Получить сеанс по ID.
     */
    @GetMapping("/{id}")
    public Session get(@PathVariable Long id) {
        log.info("Получение сеанса ID: {}", id);
        Session session = sessionService.getSessionById(id);
        log.info("Сеанс ID {} получен", id);
        return session;
    }

    /**
     * POST /api/sessions
     * Создать новый сеанс.
     */
    @PostMapping
    public Session create(@Valid @RequestBody SessionDto dto) {
        log.info("Создание сеанса: фильм {}, зал {}, время {}",
                dto.movieId(), dto.hallId(), dto.startTime());
        Session saved = sessionService.createSession(dto);
        log.info("Сеанс создан с ID {}", saved.getId());
        return saved;
    }

    /**
     * GET /api/sessions/by-date
     * Получить сеансы на указанную дату (параметр date в формате YYYY-MM-DD).
     */
    @GetMapping("/by-date")
    public List<Session> getSessionsByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Получение сеансов на дату: {}", date);
        List<Session> sessions = sessionService.getSessionsByDate(date);
        log.info("Сеансы на {} получены. Количество: {}", date, sessions.size());
        return sessions;
    }

    /**
     * DELETE /api/sessions/{id}
     * Удалить сеанс (только если нет броней).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("Удаление сеанса с ID: {}", id);
        sessionService.deleteSession(id);
        log.info("Сеанс с ID {} успешно удалён", id);
        return ResponseEntity.noContent().build();
    }
}