package com.example.moviebot.config;

import com.example.moviebot.controller.MovieBot;
import com.example.moviebot.repository.FilmsRepository;
import com.example.moviebot.service.FilmsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(MovieBot movieBot) throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(movieBot);
        return api;
    }
}