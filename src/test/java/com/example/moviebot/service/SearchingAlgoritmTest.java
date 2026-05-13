package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.repository.FilmsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchingAlgoritmTest {

    @Mock
    private FilmsRepository filmsRepository;

    @Mock
    private FilmRestService filmRestService;

    @Test
    void shouldReturnRecommendedFilms()
            throws IOException, InterruptedException {

        Films film1 = new Films();
        film1.setOriginal_title("Interstellar");
        film1.setVote_average(8.7);
        film1.setOriginal_language("en");

        Films film2 = new Films();
        film2.setOriginal_title("Bad Movie");
        film2.setVote_average(5.0);
        film2.setOriginal_language("en");

        Films film3 = new Films();
        film3.setOriginal_title("French Movie");
        film3.setVote_average(9.0);
        film3.setOriginal_language("fr");

        when(
                filmRestService.searchMovieByGenre(
                        anyList(),
                        eq(50)
                )
        ).thenReturn(
                new Films[]{
                        film1,
                        film2,
                        film3
                }
        );

        when(
                filmsRepository.isUserWatched(
                        1L,
                        "Interstellar"
                )
        ).thenReturn(false);

        SearchingAlgoritm algorithm =
                new SearchingAlgoritm(
                        filmsRepository,
                        filmRestService,
                        List.of("Action"),
                        1L
                );

        List<Films> result =
                algorithm.getFilmsForUser();

        assertEquals(1, result.size());

        assertEquals(
                "Interstellar",
                result.get(0).getOriginal_title()
        );
    }

    @Test
    void shouldNotReturnWatchedFilms()
            throws IOException, InterruptedException {

        Films film = new Films();
        film.setOriginal_title("Batman");
        film.setVote_average(8.0);
        film.setOriginal_language("en");

        when(
                filmRestService.searchMovieByGenre(
                        anyList(),
                        eq(50)
                )
        ).thenReturn(new Films[]{film});

        when(
                filmsRepository.isUserWatched(
                        1L,
                        "Batman"
                )
        ).thenReturn(true);

        SearchingAlgoritm algorithm =
                new SearchingAlgoritm(
                        filmsRepository,
                        filmRestService,
                        List.of("Action"),
                        1L
                );

        List<Films> result =
                algorithm.getFilmsForUser();

        assertTrue(result.isEmpty());
    }
}