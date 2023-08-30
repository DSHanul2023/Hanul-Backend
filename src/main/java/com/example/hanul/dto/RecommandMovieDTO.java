package com.example.hanul.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecommandMovieDTO {
    private String response;
    private List<List<Map<String, Object>>> recommendedMovies;  // Corrected type here
}
