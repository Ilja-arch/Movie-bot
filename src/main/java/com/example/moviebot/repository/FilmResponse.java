package com.example.moviebot.repository;

import com.example.moviebot.DTO.Films;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FilmResponse {
    private List<Films> results;
}
