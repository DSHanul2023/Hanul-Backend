package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDBMovieDTO {
    @JsonProperty("title")
    private String title;

    @JsonProperty("overview")
    private String overview;

    @JsonProperty("adult")
    private boolean adult;

    @JsonProperty("poster_path")
    private String posterPath; // 영화 포스터 이미지 경로

    @JsonProperty("genre_ids") // genre Id 리스트
    private List<Integer> genreIds;

    @JsonProperty("id") // tmdb의 movieId
    private String movieId;

}
