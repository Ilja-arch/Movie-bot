package repository;
import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "films")

public class FilmsEntity {
    @Id
    @EqualsAndHashCode.Include//need to rewrite as popular films(posterUrl, duration, review, voites count)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalTitle;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private String posterUrl;

}



