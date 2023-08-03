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
    private String type;
    private String title;
    private String contents;
    private String image;
    private String author;
    private String date;
    private String idx;

    public BoardDTO(final BoardEntity entity) {
        this.type = entity.getType();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.contents = entity.getContents();
        this.date = entity.getDate();
        this.image = entity.getImage();
        this.idx = entity.getIdx();
    }

    public static BoardEntity toEntity(final BoardDTO dto) {
        return BoardEntity.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .idx(dto.getIdx())
                .contents(dto.getContents())
                .author(dto.getAuthor())
                .image(dto.getImage())
                .date(dto.getDate())
                .build();
    }
}
