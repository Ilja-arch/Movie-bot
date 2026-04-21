package controller;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import repository.FilmsEntity;
import repository.GenreMapper;
import service.FilmsService;
import repository.Films;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class MovieBot extends TelegramLongPollingBot {

    private final FilmsService userService = new FilmsService();
    private Films currentFilm = new Films();

    private Map<Long, String> userLastFilm = new HashMap<>();

    public static void main(String[] args) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new MovieBot());
        System.out.println("Bot started...");
    }

    @Override
    public void onUpdateReceived(Update update) {


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
        Long userTgId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getChat().getUserName();
        switch (text) {

            case "/start" -> sendWelcome(chatId,userTgId,userName);

            case "🔍 Find films" -> send(chatId, "Type film name:");
            case "🔥 Popular" -> showPopular(chatId);
            case "⭐ My films" -> showMyFilms(userTgId);
            case "🎯 Recommend" -> showRecommendations(chatId);
            case "Back" -> sendMenu(chatId);
            case "Save film" ->
                saveFilm(chatId);
            default -> send(chatId, "❌ Unknown command");


        }
    }

    private void sendMenu(Long chatId){
        send(chatId,
                "🎬 Hello! Welcome to Movie Bot!\n\n" +
                        "🔍 Find films\n" +
                        "⭐ Save & rate\n" +
                        "🔥 Popular this week\n" +
                        "🎯 Recommendations\n\n" +
                        "Choose 👇",
                getMenu());
    }
    private void sendWelcome(Long chatId,Long tgId,String name) {
        if (userService.findByIdByTgId(chatId)==-1){
            userService.saveUser(chatId,tgId,name);
        }
        send(chatId,
                "🎬 Hello! Welcome to Movie Bot!\n\n" +
                        "🔍 Find films\n" +
                        "⭐ Save & rate\n" +
                        "🔥 Popular this week\n" +
                        "🎯 Recommendations\n\n" +
                        "Choose 👇",
                getMenu());
    }


    private void saveFilm(Long chatId) {

        if (currentFilm == null) {
            send(chatId, "❌ No film to rate");
            return;
        }

        // store film per user (IMPORTANT)
        userLastFilm.put(chatId, currentFilm.getOriginal_title());

        sendInline(
                chatId,
                "⭐ Rate this film: " + currentFilm.getOriginal_title(),
                getRatingButtons()
        );
    }
    private void handleCallback(Update update) {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data.startsWith("rate_")) {
            handleRating(chatId, data);
        }
    }
    private void handleRating(Long chatId, String data) {

        try {
            int rating = Integer.parseInt(data.replace("rate_", ""));

            String filmName = userLastFilm.get(chatId);

            if (filmName == null) {
                send(chatId, "❌ No film selected");
                return;
            }

            userService.saveWatchedFilm(rating, filmName, chatId);

            send(chatId, "✅ Saved: " + filmName + " (" + rating + "⭐)");

            userLastFilm.remove(chatId);

        } catch (Exception e) {
            send(chatId, "❌ Invalid rating");
        }
    }

    private  void showPopular(Long chatId) {
        try {
            send(chatId,"hi",getScrollMenu());

            List<Films> films = userService.getPopularFilmsThisWeek();
            GenreMapper mapper = new GenreMapper();
            int count = 1;
            for (var f : films) {
                String Text;
                currentFilm = f;
                if (f.getVote_average() > 0) {
                    Text = "\n⭐Rating " + String.format("%.1f",f.getVote_average());
                    Text+="\n\uD83D\uDE4B votes "+f.getVote_count();
                } else {
                    Text = "\n⭐ No ratings yet";
                }
                Text += "\n⏳ Duration "+userService.getMovieDuration(f.getOriginal_title());
                List<String> genList= f.getGenre_ids();
                Text += "\n\uD83C\uDFAC Genres ";
                int c = 0;
                for (String g:genList) {
                    if (c>genList.size()-2){
                        Text += mapper.getGenreName(Integer.parseInt(g));
                    }else {
                        Text += mapper.getGenreName(Integer.parseInt(g)) + ", ";
                    }
                    c++;
                }
                sendPhoto(chatId,userService.getFilmUrl(f.getOriginal_title()),"\uD83D\uDD25 "+f.getOriginal_title()+Text);
                System.out.println("\uD83D\uDD25 "+f.getOriginal_title()+"   "+f.getVote_average()+"    "+f.getVote_count()+"   "+userService.getMovieDuration(f.getOriginal_title()));
                if (count ==  3){
                    break;
                }
                count++;
            }

        } catch (Exception e) {
            send(chatId, "Error");
        }
    }
    private void sendPhoto(Long chatId, String imageUrl, String caption) {
        try {
            String apiUrl = "https://api.telegram.org/bot" + getBotToken() + "/sendPhoto";

            String params = "chat_id=" + URLEncoder.encode(chatId.toString(), "UTF-8") +
                    "&photo=" + URLEncoder.encode(imageUrl, "UTF-8") +
                    "&caption=" + URLEncoder.encode(caption, "UTF-8");

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Photo sent. Response code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
    private ReplyKeyboardMarkup getScrollMenu(){
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Next film");
        row1.add("Save film");
        row1.add("Back");
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row1));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

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
        return "BestMovie4you_bot";
    }

    private static final String BOT_TOKEN = System.getenv("botToken");
    @Override
    public String getBotToken() {return "botToken";}
}