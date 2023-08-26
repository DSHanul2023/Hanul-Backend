package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseDateDTO {
    @JsonProperty("iso_3166_1")
    private String region;

    @JsonProperty("release_dates")
    private List<ReleaseInfoDTO> release_dates;
}
