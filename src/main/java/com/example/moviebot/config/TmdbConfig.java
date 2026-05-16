package com.example.moviebot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TmdbConfig {

    @Bean
    public RestTemplate tmdbRestTemplate() {
        return new RestTemplate();
    }
}