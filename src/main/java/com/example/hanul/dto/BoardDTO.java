package com.example.hanul.dto;

import com.example.hanul.model.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private String title;
    private String contents;
    private String image;
    private String author;
    private String date;

    public BoardDTO(final BoardEntity entity) {
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.contents = entity.getContents();
        this.date = entity.getDate();
        this.image = entity.getImage();
    }

    public static BoardEntity toEntity(final BoardDTO dto) {
        return BoardEntity.builder()
                .title(dto.getTitle())
                .contents(dto.getContents())
                .author(dto.getAuthor())
                .image(dto.getImage())
                .date(dto.getDate())
                .build();
    }
}
