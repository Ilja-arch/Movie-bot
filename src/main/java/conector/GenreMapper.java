package conector;

import java.util.HashMap;
import java.util.Map;

public class GenreMapper {
    private static final Map<Integer, String> GENRES = new HashMap<>();
    private static final Map<String, Integer> GENRE_IDS = new HashMap<>();

    static {
        GENRES.put(28, "Action");
        GENRES.put(12, "Adventure");
        GENRES.put(16, "Animation");
        GENRES.put(35, "Comedy");
        GENRES.put(80, "Crime");
        GENRES.put(99, "Documentary");
        GENRES.put(18, "Drama");
        GENRES.put(10751, "Family");
        GENRES.put(14, "Fantasy");
        GENRES.put(36, "History");
        GENRES.put(27, "Horror");
        GENRES.put(10402, "Music");
        GENRES.put(9648, "Mystery");
        GENRES.put(10749, "Romance");
        GENRES.put(878, "Science Fiction");
        GENRES.put(10770, "TV Movie");
        GENRES.put(53, "Thriller");
        GENRES.put(10752, "War");
        GENRES.put(37, "Western");


        for (Map.Entry<Integer, String> entry : GENRES.entrySet()) {
            GENRE_IDS.put(entry.getValue(), entry.getKey());
        }
    }

    public static String getGenreName(int id) {
        return GENRES.getOrDefault(id, "Unknown");
    }

    public static int getGenreId(String name) {
        return GENRE_IDS.getOrDefault(name, -1);
    }
}