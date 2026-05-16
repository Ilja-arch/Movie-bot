package com.example.moviebot.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmsRepositoryTest {

    @Mock
    private FilmsRepository filmsRepository;

    private FilmsEntity film1;
    private FilmsEntity film2;
    private FilmsEntity film3;

    private final Long USER_ID_1 = 1L;
    private final Long USER_ID_2 = 2L;
    private final Long FILM_ID = 100L;

    @BeforeEach
    void setUp() {

        film1 = new FilmsEntity();
        film1.setId(1L);
        film1.setOriginalTitle("The Matrix");
        film1.setUserId(USER_ID_1);

        film2 = new FilmsEntity();
        film2.setId(2L);
        film2.setOriginalTitle("Inception");
        film2.setUserId(USER_ID_1);

        film3 = new FilmsEntity();
        film3.setId(3L);
        film3.setOriginalTitle("The Matrix");
        film3.setUserId(USER_ID_2);
    }

    @Test
    @DisplayName("Should return all films for a specific user")
    void getAllUserFilms_ShouldReturnUserFilms() {

        List<FilmsEntity> expectedFilms = Arrays.asList(film1, film2);
        when(filmsRepository.getAllUserFilms(USER_ID_1)).thenReturn(expectedFilms);


        List<FilmsEntity> actualFilms = filmsRepository.getAllUserFilms(USER_ID_1);


        assertThat(actualFilms).isNotNull();
        assertThat(actualFilms).hasSize(2);
        assertThat(actualFilms).containsExactly(film1, film2);
        verify(filmsRepository, times(1)).getAllUserFilms(USER_ID_1);
    }

    @Test
    @DisplayName("Should return empty list when user has no films")
    void getAllUserFilms_ShouldReturnEmptyList() {

        when(filmsRepository.getAllUserFilms(999L)).thenReturn(List.of());


        List<FilmsEntity> actualFilms = filmsRepository.getAllUserFilms(999L);


        assertThat(actualFilms).isNotNull();
        assertThat(actualFilms).isEmpty();
        verify(filmsRepository, times(1)).getAllUserFilms(999L);
    }

    @Test
    @DisplayName("Should return true when user has watched a specific film")
    void isUserWatched_ShouldReturnTrue() {

        when(filmsRepository.isUserWatched(USER_ID_1, "The Matrix")).thenReturn(true);


        boolean result = filmsRepository.isUserWatched(USER_ID_1, "The Matrix");


        assertTrue(result);
        verify(filmsRepository, times(1)).isUserWatched(USER_ID_1, "The Matrix");
    }

    @Test
    @DisplayName("Should return false when user hasn't watched a specific film")
    void isUserWatched_ShouldReturnFalse() {

        when(filmsRepository.isUserWatched(USER_ID_1, "Unknown Movie")).thenReturn(false);


        boolean result = filmsRepository.isUserWatched(USER_ID_1, "Unknown Movie");


        assertFalse(result);
        verify(filmsRepository, times(1)).isUserWatched(USER_ID_1, "Unknown Movie");
    }

    @Test
    @DisplayName("Should return false when different user watched the same film")
    void isUserWatched_ShouldReturnFalseForDifferentUser() {
        when(filmsRepository.isUserWatched(USER_ID_2, "Inception")).thenReturn(false);


        boolean result = filmsRepository.isUserWatched(USER_ID_2, "Inception");


        assertFalse(result);
        verify(filmsRepository, times(1)).isUserWatched(USER_ID_2, "Inception");
    }

    @Test
    @DisplayName("Should save a new film successfully")
    void saveFilm_ShouldExecuteSuccessfully() {

        doNothing().when(filmsRepository).saveFilm("Avatar", USER_ID_1);


        filmsRepository.saveFilm("Avatar", USER_ID_1);


        verify(filmsRepository, times(1)).saveFilm("Avatar", USER_ID_1);
    }

    @Test
    @DisplayName("Should save a film with different user")
    void saveFilm_ShouldSaveForDifferentUser() {

        doNothing().when(filmsRepository).saveFilm("Titanic", USER_ID_2);


        filmsRepository.saveFilm("Titanic", USER_ID_2);


        verify(filmsRepository, times(1)).saveFilm("Titanic", USER_ID_2);
    }

    @Test
    @DisplayName("Should delete film by ID successfully")
    void deleteById_ShouldExecuteSuccessfully() {

        doNothing().when(filmsRepository).deleteById(FILM_ID);


        filmsRepository.deleteById(FILM_ID);


        verify(filmsRepository, times(1)).deleteById(FILM_ID);
    }

    @Test
    @DisplayName("Should delete different films by their IDs")
    void deleteById_ShouldDeleteMultipleFilms() {

        doNothing().when(filmsRepository).deleteById(anyLong());


        filmsRepository.deleteById(1L);
        filmsRepository.deleteById(2L);
        filmsRepository.deleteById(3L);


        verify(filmsRepository, times(3)).deleteById(anyLong());
        verify(filmsRepository).deleteById(1L);
        verify(filmsRepository).deleteById(2L);
        verify(filmsRepository).deleteById(3L);
    }

    @Test
    @DisplayName("Should verify no interactions when not called")
    void verifyNoInteractions() {

        Mockito.verifyNoInteractions(filmsRepository);
    }


    @Test
    @DisplayName("Should verify method call order")
    void shouldVerifyMethodCallOrder() {

        when(filmsRepository.getAllUserFilms(USER_ID_1)).thenReturn(Arrays.asList(film1, film2));
        doNothing().when(filmsRepository).saveFilm(anyString(), anyLong());
        doNothing().when(filmsRepository).deleteById(anyLong());


        filmsRepository.getAllUserFilms(USER_ID_1);
        filmsRepository.saveFilm("New Film", USER_ID_1);
        filmsRepository.deleteById(1L);


        org.mockito.InOrder inOrder = inOrder(filmsRepository);
        inOrder.verify(filmsRepository).getAllUserFilms(USER_ID_1);
        inOrder.verify(filmsRepository).saveFilm("New Film", USER_ID_1);
        inOrder.verify(filmsRepository).deleteById(1L);
    }
}