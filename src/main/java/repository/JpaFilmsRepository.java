package repository;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    public List<FilmsEntity> getAllWatchedFilmsInApp(String filmName){
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
            try {
                return em.createQuery(
                                "SELECT f FROM FilmsEntity f WHERE f.originalTitle = :title",
                                FilmsEntity.class)
                        .setParameter("title", filmName)
                        .getResultList();
            } finally {
                em.close();
            }

    }
    public void deleteAll() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.createQuery("DELETE FROM FilmsEntity").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    public void save(FilmsEntity film){
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        System.out.println("saved "+film.getOriginalTitle());
        try {
            tx.begin();
            em.persist(film);
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



