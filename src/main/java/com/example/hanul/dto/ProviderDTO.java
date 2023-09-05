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
    @JsonProperty("link")
    private String link;

    @JsonProperty("buy")
    private List<ProviderInfoDTO> buy;

    @JsonProperty("flatrate")
    private List<ProviderInfoDTO> stream;

    @JsonProperty("rent")
    private List<ProviderInfoDTO> rent;
}
