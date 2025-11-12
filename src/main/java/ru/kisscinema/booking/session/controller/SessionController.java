package ru.kisscinema.booking.session.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.session.dto.SessionDto;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.session.service.SessionService;

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
     * Получить список всех сеансов.
     */
    @GetMapping
    public List<Session> getAll(@RequestParam(required = false) Long movieId) {
        if (movieId != null) {
            log.info("Получение сеансов для фильма с ID: {}", movieId);
        } else {
            log.info("Получение списка всех сеансов...");
        }
        List<Session> sessions = sessionService.getAllSessions(movieId);
        log.info("Список сеансов успешно получен. Количество: {}", sessions.size());
        return sessions;
    }

    /**
     * Получить информацию о сеансе по ID.
     */
    @GetMapping("/{id}")
    public Session get(@PathVariable Long id) {
        log.info("Получение сеанса с ID: {}", id);
        Session session = sessionService.getSessionById(id);
        log.info("Сеанс с ID {} успешно получен", id);
        return session;
    }

    /**
     * Создать новый сеанс.
     */
    @PostMapping
    public Session create(@Valid @RequestBody SessionDto dto) {
        log.info("Создание сеанса: фильм ID {}, зал ID {}, время {}",
                dto.movieId(), dto.hallId(), dto.startTime());
        Session saved = sessionService.createSession(dto);
        log.info("Сеанс успешно создан с ID {}", saved.getId());
        return saved;
    }
}