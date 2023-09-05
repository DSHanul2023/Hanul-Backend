package com.example.hanul.response;

import com.example.hanul.dto.CreditDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditListResponse {
    @JsonProperty("cast")
    private List<CreditDTO> cast;

    @JsonProperty("crew")
    private List<CreditDTO> crew;
}
