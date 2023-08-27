package com.example.hanul.response;

import com.example.hanul.dto.ReleaseDateDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseDateListResponse {
    @JsonProperty("results")
    private List<ReleaseDateDTO> results;
}
