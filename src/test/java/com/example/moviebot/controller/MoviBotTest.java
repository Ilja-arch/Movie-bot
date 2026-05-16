package com.example.moviebot.controller;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.repository.FilmsRepository;
import com.example.moviebot.service.FilmRestService;
import com.example.moviebot.service.FilmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieBotTest {

    @Mock
    private FilmsService filmsService;

    @Mock
    private FilmsRepository filmsRepository;

    @Mock
    private FilmRestService filmRestService;

    @InjectMocks
    private MovieBot movieBot;

    @Test
    void shouldHandleStartCommand() {

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getFrom()).thenReturn(user);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(456L);

        when(user.getId()).thenReturn(123L);
        when(user.getUserName()).thenReturn("Alex");


        movieBot.onUpdateReceived(update);


        verify(filmsService).registerUser(123L, 456L, "Alex");
    }

    @Test
    void shouldHandlePopularButton() {

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("🔥 Popular");
        when(message.getFrom()).thenReturn(user);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(456L);

        when(user.getId()).thenReturn(123L);

        when(filmsService.getPopularFilmsThisWeek())
                .thenReturn(Collections.emptyList());
        when(filmsService.getWatchedFilms(123L))
                .thenReturn(Collections.emptyList());


        movieBot.onUpdateReceived(update);


        verify(filmsService).getPopularFilmsThisWeek();
    }

    @Test
    void shouldHandleMyFilmsButton() {

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("⭐ My films");
        when(message.getFrom()).thenReturn(user);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(456L);

        when(user.getId()).thenReturn(123L);

        when(filmsService.getWatchedFilms(123L))
                .thenReturn(Collections.emptyList());


        movieBot.onUpdateReceived(update);


        verify(filmsService).getWatchedFilms(123L);
    }

    @Test
    void shouldHandleSearchFilmsButton() {

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("🎯 Search films");
        when(message.getFrom()).thenReturn(user);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(456L);

        when(user.getId()).thenReturn(123L);


        movieBot.onUpdateReceived(update);


        verifyNoInteractions(filmsService);
    }

    @Test
    void shouldHandleDefaultMessage() {

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        Chat chat = mock(Chat.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("Random text");
        when(message.getFrom()).thenReturn(user);
        when(message.getChat()).thenReturn(chat);
        when(message.getChatId()).thenReturn(456L);

        when(user.getId()).thenReturn(123L);


        movieBot.onUpdateReceived(update);


        verifyNoInteractions(filmsService);
    }

    @Test
    void shouldHandleSaveCallback() {
        // Подготовка
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("SAVE");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(456L);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(123L);


        try {
            java.lang.reflect.Field field = MovieBot.class.getDeclaredField("userCurrentFilm");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Long, Films> map = (java.util.Map<Long, Films>) field.get(movieBot);

            Films testFilm = new Films();
            testFilm.setOriginal_title("Test Movie");
            testFilm.setId(1L);
            map.put(456L, testFilm);
        } catch (Exception e) {
            e.printStackTrace();
        }


        movieBot.onUpdateReceived(update);


        verify(filmsService).saveWatchedFilm("Test Movie", 123L);
    }
}