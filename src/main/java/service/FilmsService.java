package service;


import jakarta.persistence.Persistence;
import repository.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FilmsService {
    private long tgId;
    private long chatId;
    private String name;

    public void UserService(long tgId, long chatId) {
        this.tgId = tgId;
        this.chatId = chatId;
        JpaUserRepository users = new JpaUserRepository();
        if (users.findByTgId(tgId) == null) {
            System.out.println("user is not found by id");
            UserEntity user = new UserEntity(null, tgId, name, chatId);
            users.save(user);
        } else {
            UserEntity totalUser = users.findByTgId(tgId);

        }

    }

    public List<Films> getFilmsForUser(List<String> genres, long userId)
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
        Films[] films = restApi.searchMovieByGenre(genIds, 20);
        List<Films> recomendList = new ArrayList<>();
        for (Films film : films) {
            if (film.getVote_average() > 7.5) {
                if (("en").equals(film.getOriginal_language())) {
                    if (!filmsRepository.isUserWatched(film.getOriginal_title(), userId)) {
                        List<FilmsEntity> WatchedFilmsEntities = filmsRepository.getAllWatchedFilmsInApp(film.getOriginal_title());
                        if (!WatchedFilmsEntities.isEmpty()) {
                            double avgUsersRating = 0.0D;
                            double filmsCount = 0;
                            for (FilmsEntity i : WatchedFilmsEntities) {
                                if (i.getReview() > 7 && i.getUserId() != userId) {
                                    avgUsersRating += i.getReview();
                                    filmsCount++;
                                }
                            }
                            avgUsersRating = avgUsersRating / filmsCount;
                            if (avgUsersRating > 7) {
                                recomendList.add(film);
                                continue;
                            } else {
                                double totalRating = avgUsersRating + film.getVote_average() / 2;
                                if (totalRating > 7) {
                                    recomendList.add(film);
                                    continue;
                                }
                            }
                        }
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
    public String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;

        return hours + "h " + mins + "m";
    }
    public String getMovieDuration(String movieName){
        RestApi restApi = new RestApi();
        return formatDuration(restApi.getMovieDuration(movieName));
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
    public void saveUser(Long chatId,Long userTgId,String name){
        JpaUserRepository user = new JpaUserRepository();
        UserEntity entity = new UserEntity(null,userTgId,name,chatId);
        user.save(entity);
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
    public static String getFilmUrl(String movieName) {
        RestApi restApi = new RestApi();
        return restApi.getPosterUrl(movieName);
    }

    public Films[] getWatchedFilms(long tgId) {
        try {
            long userId = findByIdByTgId(tgId);
            JpaFilmsRepository filmsRepository = new JpaFilmsRepository();

            List<FilmsEntity> filmsList = filmsRepository.getAllUserFilms(userId);

            if (filmsList.isEmpty()) {
                return new Films[0];
            }

            Films[] result = new Films[filmsList.size()];

            for (int i = 0; i < filmsList.size(); i++) {
                FilmsEntity entity = filmsList.get(i);

                Films film = new Films();
                film.setOriginal_title(entity.getOriginalTitle()); // adjust field names!
                film.setVote_average(entity.getReview());

                result[i] = film;
            }

            return result;

        } catch (Exception ex) {
            ex.printStackTrace(); // 🔥 don't hide errors
            return new Films[0];  // better than null
        }
    }

    public long findByIdByTgId(long tgId) {
        try {

            JpaUserRepository jpaUserRepository = new JpaUserRepository();
            UserEntity user = jpaUserRepository.findByTgId(tgId);
            return user.getId();
        } catch (Exception ex) {
            System.out.println("Error");
            return -1;
        }
    }
}

