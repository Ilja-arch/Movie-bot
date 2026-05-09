package com.example.moviebot.repository;
import org.springframework.data.jpa.repository.Modifying; // <--- Check this!
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface FilmsRepository extends JpaRepository<FilmsEntity, Long> {

    @Query("FROM FilmsEntity f WHERE f.userId = :userId")
    List<FilmsEntity> getAllUserFilms(@Param("userId") long userId);


    @Query("""
       SELECT COUNT(f) > 0
       FROM FilmsEntity f
       WHERE f.userId = :userId
       AND f.originalTitle = :filmName
       """)
    boolean isUserWatched(@Param("userId") long userId,

                          @Param("filmName") String filmName);
    @Modifying
    @Query(value = "INSERT INTO films (original_title, user_id) VALUES (:originalTitle, :userId)", nativeQuery = true)
    void saveFilm(@Param("originalTitle") String originalTitle, @Param("userId") Long userId);


    @Modifying
    @Transactional
    @Query("DELETE FROM FilmsEntity f WHERE f.id = :id")
    void deleteById(@Param("id") Long id);
}