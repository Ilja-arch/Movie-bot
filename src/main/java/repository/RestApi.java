package repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RestApi {

    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String API_KEY = "2b1c264e855785a9f115ce89b7a4d495";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RestApi() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public List<Films> getTrendingWeek() throws IOException, InterruptedException {

        String url = "https://api.themoviedb.org/3/trending/movie/week?api_key=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode());
        }

        FilmResponse filmResponse = objectMapper.readValue(
                response.body(), FilmResponse.class);

        return filmResponse.getResults();
    }


    public Films[] searchMovieByGenre(List<Integer> genIds,int pages)
            throws IOException, InterruptedException {

        if (genIds == null || genIds.isEmpty()) {
            return new Films[0];
        }


        String genres = String.join(",",
                genIds.stream().map(String::valueOf).toList());

        List<Films> allFilms = new ArrayList<>();

        for (int i = 1; i <= pages; i++) {

            String url = "https://api.themoviedb.org/3/discover/movie?api_key="
                    + API_KEY + "&with_genres=" + genres + "&page=" + i;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP error code: " + response.statusCode());
            }

            FilmResponse filmResponse = objectMapper.readValue(
                    response.body(), FilmResponse.class);

            allFilms.addAll(filmResponse.getResults());
        }

        return allFilms.toArray(new Films[0]);
    }


    public Films[] getFilmsByName(String movieName)
            throws IOException, InterruptedException {

        if (movieName == null || movieName.isBlank()) {
            return new Films[0];
        }

        String encodedName = URLEncoder.encode(movieName, StandardCharsets.UTF_8);

        String url = BASE_URL + "?api_key=" + API_KEY + "&query=" + encodedName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return new Films[0];
        }

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode());
        }

        FilmResponse filmResponse = objectMapper.readValue(
                response.body(), FilmResponse.class);

        return filmResponse.getResults().toArray(new Films[0]);
    }

    public String toJson(Films film) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(film);
    }
}