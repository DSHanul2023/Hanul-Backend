package com.example.hanul.response;

import com.example.hanul.dto.TMDBMovieDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TMDBMovieListResponse {
    @JsonProperty("results")
    private List<TMDBMovieDTO> results;

    @JsonProperty("total_pages")
    private int totalPages;

    // Getter and Setter for totalPages

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
