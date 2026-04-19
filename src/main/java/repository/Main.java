package repository;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {

            JpaUserRepository userRepo = new JpaUserRepository();
             JpaFilmsRepository filmRepo = new JpaFilmsRepository();


            UserEntity user = new UserEntity();

            user.setName("TestUser");
            user.setUserTgId(123456L);
            user.setChatId(123456L);

            userRepo.save(user);


            Optional<UserEntity> foundUser = userRepo.findByTgId(123456L);
            System.out.println("User found: " + foundUser.isPresent());


            FilmsEntity film = new FilmsEntity();

            film.setOriginalTitle("Inception");
            film.setUserId(123456L);
            film.setReview(9);

            filmRepo.save(film);


            boolean watched = filmRepo.isUserWatched("Inception", 123456L);
            System.out.println("Watched: " + watched);


            List<FilmsEntity> films = filmRepo.getAllUserFilms(123456L);
            System.out.println("Films count: " + films.size());

            films.forEach(f ->
                    System.out.println(f.getOriginalTitle() + " | review: " + f.getReview())
            );


        JpaUtil.close();
    }
}