package conector;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JpaFilmsRepository {


    public List<FilmsEntity> getAllUserFilms(long userId){
        EntityManager em = JpaUtil.getEntityManager();

        try {
            return em.createQuery(
                            "FROM FilmsEntity f WHERE f.userId = :userId",
                            FilmsEntity.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }
    public void save(FilmsEntity film){
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(film);
            tx.commit();
        } catch (Exception e){
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }

    }
    public boolean isUserWatched(String filmName, Long userId) {
        log.info("finding film in db");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<FilmsEntity> films = em.createQuery(
                            "FROM FilmsEntity f WHERE f.userId = :userId AND f.originalTitle = :filmName",
                            FilmsEntity.class
                    )
                    .setParameter("userId", userId)
                    .setParameter("filmName", filmName)
                    .getResultList();
            return !films.isEmpty();
        } finally {
            em.close();
        }
    }



}



