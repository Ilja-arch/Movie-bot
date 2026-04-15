package conector;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Films {
    private String original_title;
    private List<String> genre_ids;
    private String overview;
    private Double vote_average;
    private String original_language;
}
