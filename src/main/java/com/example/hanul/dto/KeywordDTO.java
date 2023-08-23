package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeywordDTO {
    @JsonProperty("id")
    private int keywordId;

    @JsonProperty("name")
    private String keywordName;
}
