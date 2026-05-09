package com.example.moviebot.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Films {
    private Long id;
    private String original_title;
    private List<String> genre_ids;
    private String overview;
    private Double vote_average;
    private Integer vote_count;
    private String original_language;
    private Double runtime;
}
