package conector;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
class FilmResponse {
    private List<Films> results;
}
