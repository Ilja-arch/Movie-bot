package service;

import repository.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilmsService {
    public Films[] getWatchedFilms(long tgId) {
        try {
            long userId = findByIdByTgId(tgId);
            JpaFilmsRepository filmsRepository = new JpaFilmsRepository();
            List<FilmsEntity> filmsList = filmsRepository.getAllUserFilms(userId);
            if (filmsList.isEmpty()) {
                return new Films[0];
            }
            return filmsList.toArray(new Films[filmsList.size()]);
        } catch (Exception ex) {
            System.out.println("error");
            return null;
        }
    }
    private long findByIdByTgId(long tgId) {
        try {

            JpaUserRepository jpaUserRepository = new JpaUserRepository();
            UserEntity user = jpaUserRepository.findByTgId(tgId);
            return user.getId();
        } catch (Exception ex) {
            System.out.println("error");
            return -1;
        }
    }
    public List<Films> getPopularFilmsThisWeek() throws IOException, InterruptedException {
        RestApi restApi = new RestApi();
        try{
            return restApi.getTrendingWeek();
        } catch (Exception ex) {
            System.out.println("error");
            return null;
        }
    }

    public void saveWatchedFilm(int review,String original_title,Long tgId) {
        try {
            long userId = findByIdByTgId(tgId);
            JpaFilmsRepository filmsRepository = new JpaFilmsRepository();
            FilmsEntity films = new FilmsEntity(null,original_title,userId,review);
            filmsRepository.save(films);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Films[] getFilmsForUser(List<String> genres,long tgId)
            throws IOException, InterruptedException {
        RestApi restApi = new RestApi();
        List<Integer> genIds = new ArrayList<>();
        JpaFilmsRepository filmsRepository = new JpaFilmsRepository();
        long userId = findByIdByTgId(tgId);
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
        FilmsService userService = new FilmsService();
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