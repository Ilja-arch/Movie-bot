package service;

import conector.Films;
import conector.GenreMapper;
import conector.RestApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public Films[] getFilmsByGenre(List<String> genres)
            throws IOException, InterruptedException {

        RestApi restApi = new RestApi();

        List<Integer> genIds = new ArrayList<>();

        for (String genre : genres) {
            int id = GenreMapper.getGenreId(genre);


            if (id != -1) {
                genIds.add(id);
            }
        }

        Films[] films = restApi.searchMovieByGenre(genIds);

        for (Films film : films) {
            System.out.println(film.getOriginal_title());
        }

        return films;
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {

        UserService userService = new UserService();

        List<String> genres = new ArrayList<>();
        genres.add("Action");
        genres.add("Adventure");

        userService.getFilmsByGenre(genres);
    }
}