package com.example.hanul.response;

import com.example.hanul.dto.TMDBMovieDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDBMovieListResponse {
    @JsonProperty("results")
    private List<TMDBMovieDTO> results;

    public List<TMDBMovieDTO> getResults() {
        return results;
    }

    public void setResults(List<TMDBMovieDTO> results) {
        this.results = results;
    }
}
