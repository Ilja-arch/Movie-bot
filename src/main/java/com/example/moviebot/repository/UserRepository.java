package com.example.moviebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(value="""
            INSERT INTO users (
            userTgId,
            name,
            chatId
            ) values (
            :userTgId,
            :name,
            :chatId)""",nativeQuery=true)
    void save(@Param("userTgId")long userTgId, @Param("name") String name, @Param("chatId") String chatId);

    @Query("FROM UserEntity u WHERE u.userTgId = :tgId")
    Optional<UserEntity> findByUserTgId(@Param("tgId") long tgId);

}
