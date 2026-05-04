package controller;

import DTO.Films;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import service.FilmsService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MovieBot extends TelegramLongPollingBot {

    private final FilmsService filmsService;

    private final Map<Long, Films> userCurrentFilm = new ConcurrentHashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("update");
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long tgId = update.getMessage().getFrom().getId();
        String username = update.getMessage().getFrom().getUserName();

        switch (text) {

            case "/start" -> {
                filmsService.registerUser(tgId, chatId, username);
                send(chatId, "🎬 Welcome!");
            }

            case "🔥 Popular" -> {
                List<Films> films = filmsService.getPopularFilmsThisWeek();

                for (Films film : films.stream().limit(3).toList()) {
                    userCurrentFilm.put(chatId, film);
                    send(chatId, film.getOriginal_title());
                }
            }

            case "Save film" -> {
                Films film = userCurrentFilm.get(chatId);
                if (film != null) {
                    filmsService.saveWatchedFilm(film.getOriginal_title(), tgId);
                    send(chatId, "Saved: " + film.getOriginal_title());
                } else {
                    send(chatId, "No film selected");
                }
            }

            case "⭐ My films" -> {
                List<Films> films = filmsService.getWatchedFilms(tgId);

                if (films.isEmpty()) {
                    send(chatId, "No films yet");
                } else {
                    films.forEach(f -> send(chatId, "⭐ " + f.getOriginal_title()));
                }
            }

            default -> send(chatId, "Unknown command");
        }
    }

    private void send(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (Exception ignored) {}
    }

    @Override
    public String getBotUsername() {
        return "BestMovie4you_bot";
    }

    @Override
    public String getBotToken() {
        return "bot-token";
    }
}