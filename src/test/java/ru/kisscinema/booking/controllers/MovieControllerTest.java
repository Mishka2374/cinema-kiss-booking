package ru.kisscinema.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.kisscinema.booking.movie.controller.MovieController;
import ru.kisscinema.booking.movie.dto.MovieDto;
import ru.kisscinema.booking.movie.service.MovieService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MovieControllerTest {

    private MockMvc mockMvc;
    private MovieService movieService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movieService = Mockito.mock(MovieService.class);
        MovieController movieController = new MovieController(movieService);
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllMovies_shouldReturnList() throws Exception {
        MovieDto movie1 = new MovieDto(1L, "Movie 1", 120, "Description 1");
        MovieDto movie2 = new MovieDto(2L, "Movie 2", 90, "Description 2");

        when(movieService.getAllMovies()).thenReturn(List.of(movie1, movie2));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Movie 1"))
                .andExpect(jsonPath("$[1].title").value("Movie 2"));

        verify(movieService, times(1)).getAllMovies();
    }

    @Test
    void getMovieById_shouldReturnMovie() throws Exception {
        MovieDto movie = new MovieDto(1L, "Movie 1", 120, "Description 1");
        when(movieService.getMovieById(1L)).thenReturn(movie);

        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Movie 1"));

        verify(movieService, times(1)).getMovieById(1L);
    }

    @Test
    void createMovie_shouldReturnCreatedMovie() throws Exception {
        MovieDto movie = new MovieDto(null, "New Movie", 110, "New Description");
        MovieDto savedMovie = new MovieDto(1L, "New Movie", 110, "New Description");

        when(movieService.createMovie(any(MovieDto.class))).thenReturn(savedMovie);

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Movie"));

        verify(movieService, times(1)).createMovie(any(MovieDto.class));
    }

    @Test
    void deleteMovie_shouldReturnNoContent() throws Exception {
        doNothing().when(movieService).deleteMovie(1L);

        mockMvc.perform(delete("/api/movies/1"))
                .andExpect(status().isNoContent());

        verify(movieService, times(1)).deleteMovie(1L);
    }
}

