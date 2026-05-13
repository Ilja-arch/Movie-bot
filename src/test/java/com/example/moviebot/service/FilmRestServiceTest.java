package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmRestServiceTest {

    private final FilmRestService filmRestService = new FilmRestService();

    @Test
    void shouldReturnMovieId() {

        String movieId = filmRestService.getMovieId("Interstellar");

        assertNotNull(movieId);
        assertFalse(movieId.isEmpty());
    }

    @Test
    void shouldReturnPosterUrl() {

        String posterUrl =
                filmRestService.getPosterUrl("Interstellar");

        assertNotNull(posterUrl);

        assertTrue(
                posterUrl.startsWith("https://image.tmdb.org/")
        );
    }

    @Test
    void shouldReturnMovieDuration() {

        Integer duration =
                filmRestService.getMovieDuration("Interstellar");

        assertNotNull(duration);

        assertTrue(duration > 0);
    }

    @Test
    void shouldReturnTrendingMovies()
            throws IOException, InterruptedException {

        List<Films> films =
                filmRestService.getTrendingWeek();

        assertNotNull(films);

        assertFalse(films.isEmpty());
    }

    @Test
    void shouldReturnFilmsByGenre()
            throws IOException, InterruptedException {

        Films[] films =
                filmRestService.searchMovieByGenre(
                        List.of(28),
                        1
                );

        assertNotNull(films);

        assertTrue(films.length > 0);
    }

    @Test
    void shouldReturnFilmByName() {

        Films film =
                filmRestService.getFilmsByName("Interstellar");

        assertNotNull(film);

        assertEquals(
                "Interstellar",
                film.getOriginal_title()
        );
    }
}