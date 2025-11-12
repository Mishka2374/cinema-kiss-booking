package ru.kisscinema.booking.movie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.movie.dto.MovieDto;
import ru.kisscinema.booking.movie.service.MovieService;

import java.util.List;

/**
 * REST контроллер для управления фильмами.
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private static final Logger log = LoggerFactory.getLogger(MovieController.class);
    private final MovieService movieService;

    /**
     * GET /api/movies
     * Получить список всех фильмов.
     */
    @GetMapping
    public List<MovieDto> getAll() {
        log.info("Получение списка фильмов...");
        List<MovieDto> movies = movieService.getAllMovies();
        log.info("Фильмы получены. Количество: {}", movies.size());
        return movies;
    }

    /**
     * GET /api/movies/{id}
     * Получить фильм по ID.
     */
    @GetMapping("/{id}")
    public MovieDto get(@PathVariable Long id) {
        log.info("Получение фильма ID: {}", id);
        MovieDto movie = movieService.getMovieById(id);
        log.info("Фильм ID {} получен", id);
        return movie;
    }

    /**
     * POST /api/movies
     * Добавить новый фильм.
     */
    @PostMapping
    public MovieDto create(@Valid @RequestBody MovieDto dto) {
        log.info("Создание фильма: {}", dto.title());
        MovieDto saved = movieService.createMovie(dto);
        log.info("Фильм '{}' создан с ID {}", dto.title(), saved.id());
        return saved;
    }

    /**
     * DELETE /api/movies/{id}
     * Удалить фильм (только если нет сеансов).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        log.info("Удаление фильма с ID: {}", id);
        movieService.deleteMovie(id);
        log.info("Фильм с ID {} успешно удалён", id);
        return ResponseEntity.noContent().build();
    }
}