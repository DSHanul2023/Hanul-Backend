package com.example.hanul.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecommandMovieDTO {
    private String response;
    private List<Map<String, Object>> recommendedMovies;
}
