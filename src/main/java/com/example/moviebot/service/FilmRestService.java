package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.DTO.FilmResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilmRestService {

    private static final String API_KEY = "2b1c264e855785a9f115ce89b7a4d495";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // Настройка тайм-аутов на уровне клиента (Connect Timeout)
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Универсальный метод для выполнения GET запросов с тайм-аутом
     */
    private Optional<String> sendRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5)) // Request Timeout
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(response.body());
            } else {
                log.warn("TMDB API returned error code: {} for URL: {}", response.statusCode(), url);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Request failed for URL: {}. Error: {}", url, e.getMessage());
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    public String getPosterUrl(String movieName) {
        String movieId = getMovieId(movieName);
        if (movieId == null) return null;

        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY;
        return sendRequest(url)
                .map(body -> {
                    JSONObject json = new JSONObject(body);
                    String path = json.optString("poster_path", null);
                    return (path != null) ? "https://image.tmdb.org/t/p/w500" + path : null;
                })
                .orElse(null);
    }

    public String getMovieId(String movieName) {
        String query = URLEncoder.encode(movieName, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + query;

        return sendRequest(url)
                .map(body -> {
                    JSONArray results = new JSONObject(body).getJSONArray("results");
                    return results.length() > 0 ? String.valueOf(results.getJSONObject(0).getInt("id")) : null;
                })
                .orElse(null);
    }

    public List<Films> getTrendingWeek() {
        String url = BASE_URL + "/trending/movie/week?api_key=" + API_KEY;
        return sendRequest(url)
                .map(body -> {
                    try {
                        return objectMapper.readValue(body, FilmResponse.class).getResults();
                    } catch (IOException e) {
                        log.error("JSON parsing error", e);
                        return null;
                    }
                })
                .orElse(new ArrayList<>());
    }

    public Films[] searchMovieByGenre(List<Integer> genIds, int pages) {
        if (genIds == null || genIds.isEmpty()) return new Films[0];

        String genres = String.join(",", genIds.stream().map(String::valueOf).toList());
        List<Films> allFilms = new ArrayList<>();


        int maxPages = Math.min(pages, 5);

        for (int i = 1; i <= maxPages; i++) {
            String url = BASE_URL + "/discover/movie?api_key=" + API_KEY + "&with_genres=" + genres + "&page=" + i;
            sendRequest(url).ifPresent(body -> {
                try {
                    allFilms.addAll(objectMapper.readValue(body, FilmResponse.class).getResults());
                } catch (IOException e) {
                    log.error("Error parsing genre page {}", e);
                }
            });
        }
        return allFilms.toArray(new Films[0]);
    }

    public Integer getMovieDuration(String movieName) {
        String movieId = getMovieId(movieName);
        if (movieId == null) return null;

        String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY;
        return sendRequest(url)
                .map(body -> new JSONObject(body).optInt("runtime", 0))
                .orElse(null);
    }

    public Films getFilmsByName(String name) {
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + encoded;

        // Вместо RestTemplate используем наш безопасный метод
        return sendRequest(url)
                .map(body -> {
                    try {
                        FilmResponse res = objectMapper.readValue(body, FilmResponse.class);
                        return (res.getResults() != null && !res.getResults().isEmpty()) ? res.getResults().get(0) : null;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElse(null);
    }
}