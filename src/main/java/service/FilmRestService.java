package service;

import DTO.Films;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import repository.FilmResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FilmRestService {

    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String API_KEY = "2b1c264e855785a9f115ce89b7a4d495";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FilmRestService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public String getPosterUrl(String movieName) {
        try {
            String movieId = getMovieId(movieName);

            if (movieId == null) return null;

            String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) return null;

            JSONObject json = new JSONObject(response.body());
            String posterPath = json.optString("poster_path", null);

            if (posterPath == null) return null;

            return "https://image.tmdb.org/t/p/w500" + posterPath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getMovieId(String movieName){
        try {
            String query = URLEncoder.encode(movieName, "UTF-8");
            String apiUrl = "https://api.themoviedb.org/3/search/movie?api_key="
                    + API_KEY + "&query=" + query;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray results = json.getJSONArray("results");

            if (results.length() > 0) {
                JSONObject firstMovie = results.getJSONObject(0);
                return String.valueOf(firstMovie.getInt("id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
    public Integer getMovieDuration(String movieName) {
        try {
            String movieId = getMovieId(movieName);
            if (movieId == null) return null;

            String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) return null;

            JSONObject json = new JSONObject(response.body());
            return json.optInt("runtime", 0);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}