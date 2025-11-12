package ru.kisscinema.booking.movie.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * Получить список всех фильмов.
     */
    @GetMapping
    public List<MovieDto> getAll() {
        log.info("Получение списка всех фильмов...");
        List<MovieDto> movies = movieService.getAllMovies();
        log.info("Список фильмов успешно получен. Количество: {}", movies.size());
        return movies;
    }

    /**
     * Получить информацию о фильме по ID.
     */
    @GetMapping("/{id}")
    public MovieDto get(@PathVariable Long id) {
        log.info("Получение фильма с ID: {}", id);
        MovieDto movie = movieService.getMovieById(id);
        log.info("Фильм с ID {} успешно получен", id);
        return movie;
    }

    /**
     * Добавить новый фильм.
     */
    @PostMapping
    public MovieDto create(@Valid @RequestBody MovieDto dto) {
        log.info("Создание нового фильма: {}", dto.title());
        MovieDto saved = movieService.createMovie(dto);
        log.info("Фильм '{}' успешно создан с ID {}", saved.title(), saved.id());
        return saved;
    }
}