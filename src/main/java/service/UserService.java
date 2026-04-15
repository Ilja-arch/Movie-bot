package service;

import conector.*;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public Films[] getFilmsForUser(List<String> genres,long userId)
            throws IOException, InterruptedException {
        RestApi restApi = new RestApi();
        List<Integer> genIds = new ArrayList<>();
        JpaFilmsRepository filmsRepository = new JpaFilmsRepository();

        for (String genre : genres) {
            int id = GenreMapper.getGenreId(genre);
            if (id != -1) {
                genIds.add(id);
            }
        }
        Films[] films = restApi.searchMovieByGenre(genIds,20);
        for (Films film : films) {

            if (film.getVote_average()>7.5){
                if (("en" ).equals(film.getOriginal_language())) {
                    if (!filmsRepository.isUserWatched(film.getOriginal_title(),userId)) {
                        List<FilmsEntity> WatchedFilmsEntities = filmsRepository.getAllWatchedFilmsInApp(film.getOriginal_title());
                        if (!WatchedFilmsEntities.isEmpty()) {
                            double avgUsersRating = 0.0D;
                            double filmsCount = 0;
                            for (FilmsEntity i : WatchedFilmsEntities) {
                                if (i.getReview() > 7&&i.getUserId()!=userId) {
                                    avgUsersRating += i.getReview();
                                    filmsCount++;
                                }
                            }
                            avgUsersRating = avgUsersRating/filmsCount;
                            if(avgUsersRating>7){
                                //recomend
                            } else{
                                //avgUsers raiting and avgDataBaseRewview
                                //if lower than 7, dont recomend
                            }
                        }
                        //System.out.println(film.getOriginal_title());
                        //System.out.println(film.getVote_average());
                    }
                }
            }
        }
        return films;
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {
        JpaFilmsRepository filmsRepository = new JpaFilmsRepository();
        FilmsEntity filmsEntity1 = new FilmsEntity(null,"Project Hail Mary",1L,10);
        FilmsEntity filmsEntity2 = new FilmsEntity(null,"Spider-Man: No Way Home",1L,10);
        FilmsEntity filmsEntity3 = new FilmsEntity(null,"The Avengers",1L,10);
        FilmsEntity filmsEntity12 = new FilmsEntity(null,"Project Hail Mary",2L,8);
        FilmsEntity filmsEntity22 = new FilmsEntity(null,"Spider-Man: No Way Home",2L,8);
        FilmsEntity filmsEntity32 = new FilmsEntity(null,"The Avengers",2L,8);
        List<FilmsEntity> list = new ArrayList<>();
        JpaUserRepository user = new JpaUserRepository();

        filmsRepository.save(filmsEntity1);
        filmsRepository.save(filmsEntity2);
        filmsRepository.save(filmsEntity3);
        UserEntity use1 = new UserEntity(null,24334L,"Irina",123123L);
        UserEntity use = new UserEntity(null,2435L,"Ilja",23433L);
        user.save(use);
        user.save(use1);
        UserService userService = new UserService();
        List<String> genres = new ArrayList<>();
        genres.add("Adventure");
        genres.add("Science Fiction");
        list = filmsRepository.getAllUserFilms(1L);
        for (FilmsEntity fil: list) {
            System.out.println(fil.getOriginalTitle());
            System.out.println(list.size());
        }

        userService.getFilmsForUser(genres,1L);
        filmsRepository.deleteAll();
        user.deleteAll();
    }
}