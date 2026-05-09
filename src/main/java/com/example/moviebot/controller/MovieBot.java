package com.example.moviebot.controller;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.DTO.GenreMapper;
import com.example.moviebot.repository.FilmsEntity;
import com.example.moviebot.repository.FilmsRepository;
import com.example.moviebot.service.FilmRestService;
import com.example.moviebot.service.FilmsService;
import com.example.moviebot.service.SearchingAlgoritm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieBot extends TelegramLongPollingBot {

    private final FilmsService filmsService;
    private final FilmsRepository filmsRepository;
    private final FilmRestService filmRestService;
    private final Map<Long, List<Films>> savedFilmsMap = new ConcurrentHashMap<>();
    private final Map<Long, Integer> savedFilmIndex = new ConcurrentHashMap<>();
    private final Map<Long, List<Films>> userFilmsList = new ConcurrentHashMap<>();
    private final Map<Long, Integer> userFilmIndex = new ConcurrentHashMap<>();
    private final Map<Long, Films> userCurrentFilm = new ConcurrentHashMap<>();
    private final Map<Long, Set<Integer>> userGenres = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        return "BestMovie4you_bot";
    }

    @Override
    public String getBotToken() {
        return "";
    }


    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Update handling error", e);
        }
    }


    private void handleMessage(Message message) throws IOException, InterruptedException {
        String text = message.getText();
        Long chatId = message.getChatId();
        Long tgId = message.getFrom().getId();

        switch (text) {
            case "/start" -> {
                filmsService.registerUser(tgId, chatId, message.getFrom().getUserName());
                sendMenu(chatId);
            }
            case "🔥 Popular" -> sendPopular(chatId);
            case "⭐ My films" -> sendSavedList(chatId, tgId);
            case "🎯 Search films" -> {
                clearUserContext(chatId);
                sendGenreMenu(chatId);
            }
            default -> send(chatId, "Please use the menu buttons below 👇");
        }
    }


    private void handleCallback(CallbackQuery callback) throws IOException, InterruptedException {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();
        Long tgId = callback.getFrom().getId();

        if (data.startsWith("GENRE_")) {
            int genreId = Integer.parseInt(data.split("_")[1]);
            userGenres.computeIfAbsent(chatId, k -> new HashSet<>()).add(genreId);
            send(chatId, "Genre added! Keep adding or press Start Search 🚀");
        }
        else if (data.equals("START_SEARCH")) {
            startRecommendation(chatId, tgId);
        }
        else if (data.equals("SAVE")) {
            Films film = userCurrentFilm.get(chatId);
            if (film != null) {
                filmsService.saveWatchedFilm(film.getOriginal_title(), tgId);
                send(chatId, "Saved to favorites ⭐");
                nextSearchFilm(chatId);
            }
        }
        else if (data.equals("SKIP"))  {
            nextSearchFilm(chatId);
        }
        else if (data.equals("DELETE")) {
            List<Films> list = savedFilmsMap.get(chatId);
            int idx = savedFilmIndex.getOrDefault(chatId, 0);

            if (list == null || idx >= list.size()) {
                send(chatId, "No film selected to delete.");
                return;
            }

            Films filmToDelete = list.get(idx);

            Long id = filmToDelete.getId();

            log.info("Deleting film: {} with ID: {}", filmToDelete.getOriginal_title(), id);


            System.out.println(id);
            filmsService.deleteFilm(id);

            send(chatId, "Deleted \"" + filmToDelete.getOriginal_title() + "\" from your list 🗑");

            list.remove(idx);

            showSavedFilm(chatId);
        }
       else {
            int nextIdx = savedFilmIndex.getOrDefault(chatId, 0) + 1;
            savedFilmIndex.put(chatId, nextIdx);
            showSavedFilm(chatId);
        }
    }


    public void sendFilm(Films film, long chatId, InlineKeyboardMarkup markup) {
        if (film == null) return;

        StringBuilder details = new StringBuilder("🔥 " + film.getOriginal_title() + "\n");

        if (film.getVote_average() != null && film.getVote_average() > 0) {
            details.append("⭐ Rating: ").append(String.format("%.1f", film.getVote_average())).append("\n");
        }

        String duration = filmsService.getMovieDuration(film.getOriginal_title());
        if (duration != null) details.append("⏳ Duration: ").append(duration).append("\n");

        if (film.getGenre_ids() != null) {
            String genreString = film.getGenre_ids().stream()
                    .map(id -> GenreMapper.getGenreName(Integer.parseInt(id)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            if (!genreString.isEmpty()) details.append("🎬 Genres: ").append(genreString);
        }

        String imageUrl = filmsService.getFilmUrl(film.getOriginal_title());
        sendContent(chatId, imageUrl, details.toString(), markup);
    }

    private void sendContent(Long chatId, String imageUrl, String text, InlineKeyboardMarkup markup) {
        try {
            if (imageUrl != null && !imageUrl.isBlank()) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(chatId.toString());
                photo.setPhoto(new InputFile(imageUrl));
                photo.setCaption(text);
                photo.setReplyMarkup(markup);
                execute(photo);
            } else {
                sendTextWithMarkup(chatId, text, markup);
            }
        } catch (Exception e) {
            log.warn("Failed to send photo, falling back to text: {}", e.getMessage());
            sendTextWithMarkup(chatId, text + "\n\n(Poster unavailable)", markup);
        }
    }


    private void startRecommendation(Long chatId, Long tgId) throws IOException, InterruptedException {
        Set<Integer> genres = userGenres.get(chatId);
        if (genres == null || genres.isEmpty()) {
            send(chatId, "Please select at least one genre first!");
            return;
        }

        List<String> genreNames = genres.stream()
                .map(GenreMapper::getGenreName)
                .collect(Collectors.toList());

        SearchingAlgoritm algorithm = new SearchingAlgoritm(filmsRepository, genreNames, filmsService.getUserIdByTgId(tgId));
        List<Films> results = algorithm.getFilmsForUser();

        if (results == null || results.isEmpty()) {
            send(chatId, "No films found for these genres. Try different ones! 😢");
            return;
        }

        userFilmsList.put(chatId, results);
        userFilmIndex.put(chatId, 0);
        showSearchFilm(chatId);
    }

    private void showSearchFilm(Long chatId) {
        List<Films> list = userFilmsList.get(chatId);
        int idx = userFilmIndex.getOrDefault(chatId, 0);

        if (list == null || idx >= list.size()) {
            send(chatId, "No more recommendations left 🎬");
            return;
        }

        Films film = list.get(idx);
        userCurrentFilm.put(chatId, film);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                List.of(btn("💾 Save", "SAVE"),
                        btn("⏭ Skip", "SKIP"))
        ));

        sendFilm(film, chatId, markup);
    }

    private void nextSearchFilm(Long chatId) {
        userFilmIndex.put(chatId, userFilmIndex.getOrDefault(chatId, 0) + 1);
        showSearchFilm(chatId);
    }

    private void sendSavedList(Long chatId, Long tgId) throws IOException, InterruptedException {
        List<Films> saved = filmsService.getWatchedFilms(tgId);
        if (saved == null || saved.isEmpty()) {
            send(chatId, "Your watchlist is empty! 📭");
            return;
        }

        savedFilmsMap.put(chatId, saved);
        savedFilmIndex.put(chatId, 0);
        send(chatId, "Found " + saved.size() + " films in your list:");
        showSavedFilm(chatId);
    }

    private void showSavedFilm(Long chatId) {
        try {
            List<Films> list = savedFilmsMap.get(chatId);
            int idx = savedFilmIndex.getOrDefault(chatId, 0);

            if (list == null || idx >= list.size()) {
                send(chatId, "That's the end of your saved list 🎬");
                return;
            }

            Films basicInfo = list.get(idx);
            System.out.println("Attempting to show saved film: " + basicInfo.getOriginal_title());


            Films detailed = null;
            try {
                detailed = filmRestService.getFilmsByName(basicInfo.getOriginal_title());
            } catch (Exception e) {
                System.err.println("API Detail fetch failed: " + e.getMessage());
            }

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                    List.of(btn("⏭ Next Saved", "NEXT_SAVED")),
                    List.of(btn("Delete","DELETE"))
            ));


            sendFilm(detailed != null ? detailed : basicInfo, chatId, markup);

        } catch (Exception e) {
            e.printStackTrace();
            send(chatId, "Error loading saved film info.");
        }
    }

    private void sendMenu(Long chatId) {
        ReplyKeyboardMarkup rm = new ReplyKeyboardMarkup(List.of(
                new KeyboardRow(List.of(new KeyboardButton("🔥 Popular"),
                new KeyboardButton("⭐ My films"))),
                new KeyboardRow(List.of(new KeyboardButton("🎯 Search films")))
        ));
        rm.setResizeKeyboard(true);

        SendMessage sm = new SendMessage(chatId.toString(), "Main Menu:");
        sm.setReplyMarkup(rm);
        executeSafe(sm);
    }

    private void sendGenreMenu(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                List.of(btn("Action", "GENRE_28"), btn("Comedy", "GENRE_35")),
                List.of(btn("Drama", "GENRE_18"), btn("Horror", "GENRE_27")),
                List.of(btn("Sci-Fi", "GENRE_878"), btn("Romance", "GENRE_10749")),
                List.of(btn("Start Search 🚀", "START_SEARCH"))
        ));
        sendTextWithMarkup(chatId, "Select genres you like:", markup);
    }

    private void sendPopular(Long chatId) {
        List<Films> popular = filmsService.getPopularFilmsThisWeek();
        if (popular.isEmpty()) {
            send(chatId, "Couldn't find popular films right now.");
            return;
        }
        String list = popular.stream().limit(5)
                .map(f -> "• " + f.getOriginal_title())
                .collect(Collectors.joining("\n"));
        send(chatId, "🔥 Top 5 This Week:\n" + list);
    }


    private void clearUserContext(Long chatId) {
        userGenres.remove(chatId);
        userFilmIndex.remove(chatId);
        userFilmsList.remove(chatId);
    }

    private void send(Long chatId, String text) {
        executeSafe(new SendMessage(chatId.toString(), text));
    }

    private void sendTextWithMarkup(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage sm = new SendMessage(chatId.toString(), text);
        sm.setReplyMarkup(markup);
        executeSafe(sm);
    }

    private InlineKeyboardButton btn(String text, String data) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);
        return b;
    }

    private void executeSafe(SendMessage msg) {
        try { execute(msg); } catch (Exception e) { log.error("Execution error", e); }
    }
}