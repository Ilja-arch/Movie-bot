
package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.repository.FilmResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FilmRestService {

    private static final String API_KEY = "2b1c264e855785a9f115ce89b7a4d495";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public String getPosterUrl(String movieName) {
        try {
            String movieId = getMovieId(movieName);
            if (movieId == null) return null;

            String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return null;

            JSONObject json = new JSONObject(response.body());
            String posterPath = json.optString("poster_path", null);

            return (posterPath == null)
                    ? null
                    : "https://image.tmdb.org/t/p/w500" + posterPath;

        } catch (Exception e) {
            log.error("Poster fetch failed for {}", movieName, e);
            return null;
        }
    }

    public String getMovieId(String movieName) {
        try {
            String query = URLEncoder.encode(movieName, StandardCharsets.UTF_8);

            String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + query;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray results = json.getJSONArray("results");

            if (!results.isEmpty()) {
                return String.valueOf(results.getJSONObject(0).getInt("id"));
            }

        } catch (Exception e) {
            log.error("Movie search failed for {}", movieName, e);
        }

        return null;
    }

    public List<Films> getTrendingWeek() throws IOException, InterruptedException {

        String url = BASE_URL + "/trending/movie/week?api_key=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode());
        }

        FilmResponse filmResponse = objectMapper.readValue(
                response.body(), FilmResponse.class);

        return filmResponse.getResults();
    }

    public Films[] searchMovieByGenre(List<Integer> genIds, int pages) throws IOException, InterruptedException {
        if (genIds == null || genIds.isEmpty()) {
            return new Films[0];
        }
        String genres = String.join(",", genIds.stream().map(String::valueOf).toList());
        List<Films> allFilms = new ArrayList<>();
        for (int i = 1; i <= pages; i++) {
            String url = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&with_genres=" + genres + "&page=" + i;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP error code: " + response.statusCode());
            }
            FilmResponse filmResponse = objectMapper.readValue(response.body(), FilmResponse.class);
            allFilms.addAll(filmResponse.getResults());
        }
        return allFilms.toArray(new Films[0]);
    }

    public Integer getMovieDuration(String movieName) {
        try {
            String movieId = getMovieId(movieName);
            if (movieId == null) return null;

            String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return null;

            JSONObject json = new JSONObject(response.body());
            return json.optInt("runtime", 0);

        } catch (Exception e) {
            log.error("Duration fetch failed for {}", movieName, e);
            return null;
        }
    }

    public Films getFilmsByName(String name) {

        try {

            String encoded =
                    URLEncoder.encode(name, StandardCharsets.UTF_8);

            String url =
                    BASE_URL +
                            "/search/movie?api_key=" +
                            API_KEY +
                            "&query=" +
                            encoded;

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<FilmResponse> response =
                    restTemplate.getForEntity(url, FilmResponse.class);

            if (response.getBody() == null ||
                    response.getBody().getResults() == null ||
                    response.getBody().getResults().isEmpty()) {

                return null;
            }

            return response.getBody().getResults().get(0);

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }
}