package com.example.moviebot.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FilmResponse {
    private List<Films> results;
}
