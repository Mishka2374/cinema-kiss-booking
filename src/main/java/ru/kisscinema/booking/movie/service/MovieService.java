package ru.kisscinema.booking.movie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.movie.dto.MovieDto;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.movie.repository.MovieRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepo;

    @Transactional(readOnly = true)
    public List<MovieDto> getAllMovies() {
        return movieRepo.findAll().stream()
                .map(m -> new MovieDto(m.getId(), m.getTitle(), m.getDurationMinutes(), m.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieById(Long id) {
        Movie movie = movieRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return new MovieDto(movie.getId(), movie.getTitle(), movie.getDurationMinutes(), movie.getDescription());
    }

    @Transactional
    public MovieDto createMovie(MovieDto dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.title());
        movie.setDurationMinutes(dto.durationMinutes());
        movie.setDescription(dto.description());
        Movie saved = movieRepo.save(movie);
        return new MovieDto(saved.getId(), saved.getTitle(), saved.getDurationMinutes(), saved.getDescription());
    }
}