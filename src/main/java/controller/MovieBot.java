package controller;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import service.FilmsService;
import repository.Films;

import java.util.*;

public class MovieBot extends TelegramLongPollingBot {

    private final FilmsService userService = new FilmsService();

    // хранение состояния пользователей
    private Map<Long, String> userLastFilm = new HashMap<>();

    public static void main(String[] args) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new MovieBot());
        System.out.println("Bot started...");
    }

    @Override
    public void onUpdateReceived(Update update) {

        // 🔘 INLINE кнопки (рейтинг)
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (data.startsWith("rate_")) {
                int rating = Integer.parseInt(data.replace("rate_", ""));
                String filmName = userLastFilm.get(chatId);

                userService.saveWatchedFilm(rating, filmName, chatId);

                send(chatId, "✅ Saved: " + filmName + " (" + rating + ")");
            }
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        switch (text) {

            case "/start" -> sendWelcome(chatId);

            case "🔍 Find films" -> send(chatId, "Type film name:");
            case "🔥 Popular" -> showPopular(chatId);
            case "⭐ My films" -> showMyFilms(chatId);
            case "🎯 Recommend" -> showRecommendations(chatId);

            default -> handleInput(chatId, text);
        }
    }

    // 🎬 Welcome
    private void sendWelcome(Long chatId) {
        send(chatId,
                "🎬 Hello! Welcome to Movie Bot!\n\n" +
                        "🔍 Find films\n" +
                        "⭐ Save & rate\n" +
                        "🔥 Popular this week\n" +
                        "🎯 Recommendations\n\n" +
                        "Choose 👇",
                getMenu());
    }

    // 🔍 поиск + добавление
    private void handleInput(Long chatId, String text) {

        // add film
        if (text.startsWith("add ")) {
            String filmName = text.replace("add ", "");
            userLastFilm.put(chatId, filmName);

            sendInline(chatId, "⭐ Rate this film:", getRatingButtons());
            return;
        }

        try {
            Films[] films = userService.getFilmsForUser(List.of("Action"), chatId);

            if (films == null || films.length == 0) {
                send(chatId, "❌ No films found");
                return;
            }

            for (Films f : films) {
                send(chatId,
                        "🎬 " + f.getOriginal_title() +
                                "\n➡️ add " + f.getOriginal_title());
            }

        } catch (Exception e) {
            send(chatId, "Error");
        }
    }

    // 🔥 популярные
    private void showPopular(Long chatId) {
        try {
            var films = userService.getPopularFilmsThisWeek();

            for (var f : films) {
                send(chatId, "🔥 " + f.getOriginal_title());
            }

        } catch (Exception e) {
            send(chatId, "Error");
        }
    }

    // ⭐ мои фильмы
    private void showMyFilms(Long chatId) {

        Films[] films = userService.getWatchedFilms(chatId);

        if (films == null || films.length == 0) {
            send(chatId, "📁 No films yet");
            return;
        }

        for (Films f : films) {
            send(chatId,
                    "⭐ " + f.getOriginal_title() +
                            " | Rating: " + f.getVote_average());
        }
    }

    // 🎯 рекомендации
    private void showRecommendations(Long chatId) {

        try {
            var films = userService.getFilmsForUser(List.of("Action", "Adventure"), chatId);

            for (Films f : films) {
                send(chatId, "🎯 " + f.getOriginal_title());
            }

        } catch (Exception e) {
            send(chatId, "Error");
        }
    }

    // 📱 меню
    private ReplyKeyboardMarkup getMenu() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🔍 Find films");
        row1.add("🔥 Popular");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⭐ My films");
        row2.add("🎯 Recommend");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row1, row2));
        keyboard.setResizeKeyboard(true);

        return keyboard;
    }

    // ⭐ inline кнопки рейтинга
    private InlineKeyboardMarkup getRatingButtons() {

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(String.valueOf(i));
            btn.setCallbackData("rate_" + i);
            row1.add(btn);
        }

        for (int i = 6; i <= 10; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(String.valueOf(i));
            btn.setCallbackData("rate_" + i);
            row2.add(btn);
        }

        rows.add(row1);
        rows.add(row2);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }

    // 📤 send
    private void send(Long chatId, String text) {
        send(chatId, text, null);
    }

    private void send(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);

        if (keyboard != null) msg.setReplyMarkup(keyboard);

        try {
            execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendInline(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        msg.setReplyMarkup(markup);

        try {
            execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "SearchBestMovieForYoubot";
    }

    @Override
    public String getBotToken() {
        return "8652319476:AAHGtZliMzKi5eSaY7bZiO0p-jjU_zlQbEc";
    }
}