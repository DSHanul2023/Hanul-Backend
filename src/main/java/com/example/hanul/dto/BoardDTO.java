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
    private byte[] image;
    private String author;
    private String date;
    private String idx;
    private String memberId;
    private boolean canEdit;

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
    public BoardDTO(final BoardEntity entity) {
        this.type = entity.getType();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.contents = entity.getContents();
        this.date = entity.getDate();
        this.image = entity.getImage();
        this.idx = entity.getIdx();
        this.memberId = entity.getMemberId();
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
                .memberId(dto.getMemberId())
                .build();
    }
}