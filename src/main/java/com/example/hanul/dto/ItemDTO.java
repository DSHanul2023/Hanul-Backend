package com.example.hanul.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private String id;
    private String itemNm;
    private String itemDetail;
    private String posterUrl;
    private String genreName;
    private String director;
    private String cast;
    private String keyword;
}
