package com.example.hanul.response;

import com.example.hanul.dto.GenreDTO;
import com.example.hanul.dto.KeywordDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeywordListResponse {
    @JsonProperty("keywords")
    private List<KeywordDTO> keywords;
}
