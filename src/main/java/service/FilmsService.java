package service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import repository.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmsService {
    private long tgId;
    private long chatId;
    private String name;
    private final UserRepository userRepository;
    private final FilmsRepository filmsRepository;
    private RestApi restApi= new RestApi();
    public void FilmsService(long tgId, long chatId) {
        this.tgId = tgId;
        this.chatId = chatId;
        if (userRepository.findByUserTgId(tgId) == null) {
            System.out.println("user is not found by id");
            UserEntity user = new UserEntity(null, tgId, name, chatId);
            userRepository.save(user);
        } else {
            UserEntity totalUser = userRepository.findByUserTgId(tgId);
        }
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
    public void saveWatchedFilm(String original_title,Long tgId) {
        filmsRepository.saveFilm(original_title,tgId,restApi.get,getMovieDuration(original_title),getFilmUrl(original_title));
    }
    public void saveUser(Long chatId,Long userTgId,String name){
        UserRepository user = new UserRepository();
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
            FilmsRepository filmsRepository = new FilmsRepository();

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

            UserRepository userRepository = new UserRepository();
            UserEntity user = userRepository.findByTgId(tgId);
            return user.getId();
        } catch (Exception ex) {
            System.out.println("Error");
            return -1;
        }
    }
}

