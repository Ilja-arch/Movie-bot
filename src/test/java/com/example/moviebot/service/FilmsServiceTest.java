package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.repository.FilmsEntity;
import com.example.moviebot.repository.FilmsRepository;
import com.example.moviebot.repository.UserEntity;
import com.example.moviebot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilmsRepository filmsRepository;

    @Mock
    private FilmRestService filmRestService;

    @InjectMocks
    private FilmsService filmsService;

    @Test
    void shouldFormatDuration() {
        String result = filmsService.formatDuration(125);
        assertEquals("2h 5m", result);
    }

    @Test
    void shouldFormatDurationWithZeroMinutes() {
        String result = filmsService.formatDuration(120);
        assertEquals("2h 0m", result);
    }

    @Test
    void shouldReturnMovieDuration() {
        when(filmRestService.getMovieDuration("Interstellar"))
                .thenReturn(169);

        String result = filmsService.getMovieDuration("Interstellar");
        assertEquals("2h 49m", result);
    }

    @Test
    void shouldReturnPosterUrl() {
        when(filmRestService.getPosterUrl("Batman"))
                .thenReturn("test_url");

        String result = filmsService.getFilmUrl("Batman");
        assertEquals("test_url", result);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.findByUserTgId(123L))
                .thenReturn(Optional.empty());

        filmsService.registerUser(123L, 456L, "Alex");

        verify(userRepository, times(1))
                .save(any(UserEntity.class));
    }

    @Test
    void shouldNotRegisterExistingUser() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .name("Alex")
                .build();

        when(userRepository.findByUserTgId(123L))
                .thenReturn(Optional.of(user));

        filmsService.registerUser(123L, 456L, "Alex");

        verify(userRepository, never())
                .save(any(UserEntity.class));
    }

    @Test
    void shouldDeleteFilm() {
        filmsService.deleteFilm(1L);
        verify(filmsRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldSaveWatchedFilm() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .build();

        when(userRepository.findByUserTgId(123L))
                .thenReturn(Optional.of(user));

        filmsService.saveWatchedFilm("Interstellar", 123L);

        verify(filmsRepository, times(1))
                .saveFilm("Interstellar", 10L);
    }

    @Test
    void shouldReturnWatchedFilms() {
        UserEntity user = UserEntity.builder().id(1L).build();
        FilmsEntity entity = new FilmsEntity();
        entity.setId(5L);
        entity.setOriginalTitle("Batman");

        when(userRepository.findByUserTgId(123L))
                .thenReturn(Optional.of(user));
        when(filmsRepository.getAllUserFilms(1L))
                .thenReturn(List.of(entity));

        List<Films> result = filmsService.getWatchedFilms(123L);

        assertEquals(1, result.size());
        assertEquals("Batman", result.get(0).getOriginal_title());
    }

    @Test
    void shouldReturnPopularFilms() throws IOException, InterruptedException {
        Films film = new Films();
        film.setOriginal_title("Interstellar");

        when(filmRestService.getTrendingWeek())
                .thenReturn(List.of(film));

        List<Films> result = filmsService.getPopularFilmsThisWeek();

        assertEquals(1, result.size());
        assertEquals("Interstellar", result.get(0).getOriginal_title());
    }
}