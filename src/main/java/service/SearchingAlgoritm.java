package service;

import repository.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchingAlgoritm  {
    private FilmsRepository filmsRepository;
    private List<String> genres;
    private long userId;
    public void SearchingAlgoritm(FilmsRepository filmsRepository, List<String> genres, long userId) {
        this.filmsRepository = filmsRepository;
        this.genres = genres;
        this.userId = userId;
    }
    public List<Films> getFilmsForUser()
            throws IOException, InterruptedException {
        RestApi restApi = new RestApi();
        List<Integer> genIds = new ArrayList<>();


        for (String genre : genres) {
            int id = GenreMapper.getGenreId(genre);
            if (id != -1) {
                genIds.add(id);
            }
        }
        Films[] films = restApi.searchMovieByGenre(genIds, 20);
        List<Films> recomendList = new ArrayList<>();
        for (Films film : films) {
            if (film.getVote_average() > 7.5) {
                if (("en").equals(film.getOriginal_language())) {
                    if (!filmsRepository.isUserWatched(userId,film.getOriginal_title())) {
                        recomendList.add(film);
                        if (recomendList.size() > 3) {
                            break;
                        }
                    }

                    }
                }
            }

        return recomendList;
    }
}
