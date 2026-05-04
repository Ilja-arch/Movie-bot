package repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface FilmsRepository extends JpaRepository<FilmsEntity, Long> {

    @Query("FROM FilmsEntity f WHERE f.userId = :userId")
    List<FilmsEntity> getAllUserFilms(@Param("userId") long userId);

    @Query("DELETE FROM FilmsEntity")
    void deleteAll();

    @Query("FROM FilmsEntity f WHERE f.userId = :userId AND f.originalTitle = :filmName")
    boolean isUserWatched(@Param("userId") long userId, @Param("filmName") String filmName);

    @Query(value = """
    INSERT INTO films (
        original_title,
        user_id,
    ) VALUES (
        :originalTitle,
        :userId,
    )
    """, nativeQuery = true)
    void saveFilm(
            @Param("originalTitle") String originalTitle,
            @Param("userId") Long userId
    );




}



