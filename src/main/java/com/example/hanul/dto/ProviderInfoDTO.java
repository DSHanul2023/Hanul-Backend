package com.example.hanul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderInfoDTO {
    @JsonProperty("logo_path")
    private String logoPath;

    @JsonProperty("provider_id")
    private int providerId;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("display_priority")
    private int displayPriority;
}
