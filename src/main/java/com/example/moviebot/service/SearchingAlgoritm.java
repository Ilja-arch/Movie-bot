package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;
import com.example.moviebot.DTO.GenreMapper;
import com.example.moviebot.repository.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchingAlgoritm  {
    private FilmsRepository filmsRepository;
    private List<String> genres;
    private long userId;



    public  SearchingAlgoritm(FilmsRepository filmsRepository, List<String> genres, long userId) {
        this.filmsRepository = filmsRepository;
        this.genres = genres;
        this.userId = userId;
    }
    public List<Films> getFilmsForUser()
            throws IOException, InterruptedException {
        FilmRestService filmRestService = new FilmRestService();
        List<Integer> genIds = new ArrayList<>();


        for (String genre : genres) {
            int id = GenreMapper.getGenreId(genre);
            if (id != -1) {
                genIds.add(id);
            }
        }
        Films[] films = filmRestService.searchMovieByGenre(genIds, 50);
        List<Films> recomendList = new ArrayList<>();
        for (Films film : films) {
            if (film.getVote_average() > 7) {
                if (("en").equals(film.getOriginal_language())) {
                    if (!filmsRepository.isUserWatched(userId,film.getOriginal_title())) {
                        recomendList.add(film);
                        if (recomendList.size() > 50) {
                            break;
                        }
                    }

                    }
                }
            }

        return recomendList;
    }
}
