package ru.kisscinema.booking.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.kisscinema.booking.audit.service.AuditService;
import ru.kisscinema.booking.movie.dto.MovieDto;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.movie.repository.MovieRepository;
import ru.kisscinema.booking.movie.service.MovieService;
import ru.kisscinema.booking.session.repository.SessionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllMovies_shouldReturnList() {
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Movie 1");
        movie1.setDurationMinutes(120);
        movie1.setDescription("Desc 1");

        Movie movie2 = new Movie();
        movie2.setId(2L);
        movie2.setTitle("Movie 2");
        movie2.setDurationMinutes(90);
        movie2.setDescription("Desc 2");

        when(movieRepository.findAll()).thenReturn(List.of(movie1, movie2));

        List<MovieDto> result = movieService.getAllMovies();

        assertEquals(2, result.size());
        assertEquals("Movie 1", result.get(0).title());
        assertEquals("Movie 2", result.get(1).title());

        verify(movieRepository, times(1)).findAll();
    }

    @Test
    void getMovieById_shouldReturnMovie() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Movie 1");
        movie.setDurationMinutes(120);
        movie.setDescription("Desc");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieDto result = movieService.getMovieById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Movie 1", result.title());

        verify(movieRepository, times(1)).findById(1L);
    }

    @Test
    void getMovieById_shouldThrowException_whenNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> movieService.getMovieById(1L));
        assertEquals("Movie not found", ex.getMessage());

        verify(movieRepository, times(1)).findById(1L);
    }

    @Test
    void createMovie_shouldSaveAndReturnMovie() {
        MovieDto dto = new MovieDto(null, "New Movie", 100, "New Desc");
        Movie savedMovie = new Movie();
        savedMovie.setId(1L);
        savedMovie.setTitle("New Movie");
        savedMovie.setDurationMinutes(100);
        savedMovie.setDescription("New Desc");

        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        MovieDto result = movieService.createMovie(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("New Movie", result.title());

        verify(movieRepository, times(1)).save(any(Movie.class));
        verify(auditService, times(1)).log(eq("Movie"), eq(1L), eq("CREATE"), any(), contains("Добавлен фильм"));
    }

    @Test
    void deleteMovie_shouldDeleteMovie_whenNoSessionsExist() {
        when(sessionRepository.existsByMovieId(1L)).thenReturn(false);

        movieService.deleteMovie(1L);

        verify(movieRepository, times(1)).deleteById(1L);
        verify(auditService, times(1)).log(eq("Movie"), eq(1L), eq("DELETE"), any(), contains("Фильм удалён"));
    }

    @Test
    void deleteMovie_shouldThrowException_whenSessionsExist() {
        when(sessionRepository.existsByMovieId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> movieService.deleteMovie(1L));
        assertEquals("Невозможно удалить фильм: существуют сеансы с этим фильмом", ex.getMessage());

        verify(movieRepository, never()).deleteById(anyLong());
        verify(auditService, never()).log(anyString(), anyLong(), anyString(), any(), anyString());
    }
}
