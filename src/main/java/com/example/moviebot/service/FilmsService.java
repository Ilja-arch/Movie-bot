package com.example.moviebot.service;

import com.example.moviebot.DTO.Films;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.moviebot.repository.*;
import org.springframework.transaction.annotation.Transactional;
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

    public void registerUser(long tgId, long chatId, String name) {
        userRepository.findByUserTgId(tgId)
                .ifPresentOrElse(
                        user -> log.info("User already exists: {}", user.getName()),
                        () -> {
                            log.info("User not found. Creating new user with tgId={}", tgId);
                            UserEntity newUser = UserEntity.builder()
                                    .userTgId(tgId)
                                    .name(name)
                                    .chatId(chatId)
                                    .build();
                            userRepository.save(newUser);
                        }
                );
    }
    @Transactional
    public void deleteFilm(Long id){
        try {

            filmsRepository.deleteById(id);
        } catch (Exception e){
            System.out.println(e);
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


    @Transactional // Обязательно!
    public void saveWatchedFilm(String originalTitle, Long tgId) {
        try {
            // Сначала находим внутренний ID пользователя
            Long internalUserId = getUserIdByTgId(tgId);

            // Передаем именно внутренний ID
            filmsRepository.saveFilm(originalTitle, internalUserId);
            System.out.println("saved");
            log.info("Фильм '{}' успешно сохранен для пользователя {}", originalTitle, tgId);
        } catch (Exception ex) {
            System.out.println("error"+ex);
            log.error("Ошибка при сохранении фильма '{}' для tgId={}", originalTitle, tgId, ex);
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


    @Transactional
    public List<Films> getWatchedFilms(Long tgId) {
        UserEntity user = userRepository.findByUserTgId(tgId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FilmsEntity> entities = filmsRepository.getAllUserFilms(user.getId());

        return entities.stream().map(entity -> {
            Films dto = new Films();
            dto.setId(entity.getId());
            dto.setOriginal_title(entity.getOriginalTitle());
            return dto;
        }).collect(Collectors.toList());
    }

    public long getUserIdByTgId(long tgId) {
        return userRepository.findByUserTgId(tgId)
                .map(UserEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for tgId: " + tgId));
    }
}