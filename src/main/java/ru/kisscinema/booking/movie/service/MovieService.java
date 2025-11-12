package ru.kisscinema.booking.movie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.movie.dto.MovieDto;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.movie.repository.MovieRepository;
import ru.kisscinema.booking.session.repository.SessionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(m -> new MovieDto(m.getId(), m.getTitle(), m.getDurationMinutes(), m.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return new MovieDto(movie.getId(), movie.getTitle(), movie.getDurationMinutes(), movie.getDescription());
    }

    @Transactional
    public MovieDto createMovie(MovieDto dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.title());
        movie.setDurationMinutes(dto.durationMinutes());
        movie.setDescription(dto.description());
        Movie saved = movieRepository.save(movie);
        return new MovieDto(saved.getId(), saved.getTitle(), saved.getDurationMinutes(), saved.getDescription());
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (sessionRepository.existsByMovieId(id)) {
            throw new RuntimeException("Невозможно удалить фильм: существуют сеансы с этим фильмом");
        }
        movieRepository.deleteById(id);
    }
}