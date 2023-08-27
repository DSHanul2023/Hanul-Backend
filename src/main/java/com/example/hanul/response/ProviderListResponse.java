package com.example.hanul.response;

import com.example.hanul.dto.ProviderDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderListResponse {
    @JsonProperty("id")
    private String movieId;

    @JsonProperty("results")
    private Map<String, ProviderDTO> results;
}