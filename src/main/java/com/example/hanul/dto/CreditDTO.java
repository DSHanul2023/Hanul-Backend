package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditDTO {
    @JsonProperty("name")
    String name;

    @JsonProperty("job")
    String job;

    @JsonProperty("order")
    Integer order;
}
