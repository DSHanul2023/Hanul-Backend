package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GenreDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    // Getter and Setter methods for all fields
}
