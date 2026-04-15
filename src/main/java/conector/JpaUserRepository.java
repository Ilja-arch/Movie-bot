package conector;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class JpaUserRepository {
    public void save(UserEntity user){
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(user);
            tx.commit();
        } catch (Exception e){
            if (tx.isActive()) tx.rollback();
            throw e;
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
    public Optional<UserEntity> findByTgId(long tgId){
        log.info("finding user by tgId: {}", tgId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "FROM UserEntity u WHERE u.userTgId = :tgId",
                            UserEntity.class
                    )
                    .setParameter("tgId", tgId)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }




    
}
