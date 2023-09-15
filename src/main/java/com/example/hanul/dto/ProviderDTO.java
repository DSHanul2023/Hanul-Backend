package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderDTO {
    private String tmdb_id;

    @JsonProperty("link")
    private String link;

    @JsonProperty("buy")
    private List<ProviderInfoDTO> buy;

    @JsonProperty("flatrate")
    private List<ProviderInfoDTO> flatrate;

    @JsonProperty("rent")
    private List<ProviderInfoDTO> rent;
}
