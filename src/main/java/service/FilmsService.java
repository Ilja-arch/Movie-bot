package service;

import DTO.Films;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import repository.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmsService  {

    private final UserRepository userRepository;
    private final FilmsRepository filmsRepository;
    private final FilmRestService filmRestService;

    // Register user if not exists
    public void registerUser(long tgId, long chatId, String name) {
        UserEntity user = userRepository.findByUserTgId(tgId);

        if (user == null) {
            log.info("User not found. Creating new user with tgId={}", tgId);
            userRepository.save(new UserEntity(null, tgId, name, chatId));
        }
    }


    public String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours + "h " + mins + "m";
    }


    public String getMovieDuration(String movieName) {
        try {
            int duration = filmRestService.getMovieDuration(movieName);
            return formatDuration(duration);
        } catch (Exception ex) {
            log.error("Failed to get duration for movie: {}", movieName, ex);
            return "Unknown";
        }
    }


    public void saveWatchedFilm(String originalTitle, Long tgId) {
        try {
            filmsRepository.saveFilm(originalTitle, tgId);
        } catch (Exception ex) {
            log.error("Error saving film '{}' for tgId={}", originalTitle, tgId, ex);
        }
    }


    public List<Films> getPopularFilmsThisWeek() {
        try {
            return filmRestService.getTrendingWeek();
        } catch (IOException | InterruptedException ex) {
            log.error("Error fetching trending films", ex);
            return List.of();
        } catch (Exception ex) {
            log.error("Unexpected error fetching trending films", ex);
            return List.of();
        }
    }


    public String getFilmUrl(String movieName) {
        try {
            return filmRestService.getPosterUrl(movieName);
        } catch (Exception ex) {
            log.error("Error fetching poster for movie: {}", movieName, ex);
            return null;
        }
    }


    public List<Films> getWatchedFilms(long tgId) {
        try {
            long userId = getUserIdByTgId(tgId);

            List<FilmsEntity> filmsList = filmsRepository.getAllUserFilms(userId);

            return filmsList.stream()
                    .map(entity -> {
                        Films film = new Films();
                        film.setOriginal_title(entity.getOriginalTitle());
                        return film;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Error fetching watched films for tgId={}", tgId, ex);
            return List.of();
        }
    }


    public long getUserIdByTgId(long tgId) {
        UserEntity user = userRepository.findByUserTgId(tgId);

        if (user == null) {
            throw new IllegalArgumentException("User not found for tgId: " + tgId);
        }

        return user.getId();
    }
}