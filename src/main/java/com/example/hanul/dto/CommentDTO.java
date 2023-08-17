package com.example.hanul.dto;

import com.example.hanul.model.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private String id;
    private String text;
    private String author;
    private String boardId;
    private String memberId;
    private String date;
    private boolean canEdit;
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public static CommentDTO fromEntity(CommentEntity comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(comment.getAuthor());
        dto.setMemberId(comment.getMemberId());
        dto.setDate(comment.getDate());
        return dto;
    }
    public CommentDTO(final CommentEntity entity) {
        this.boardId = entity.getBoardId();
        this.id = entity.getId();
        this.text = entity.getText();
        this.author = entity.getAuthor();
        this.memberId = entity.getMemberId();
        this.date = entity.getDate();
    }

    public static CommentEntity toEntity(final CommentDTO dto) {
        return CommentEntity.builder()
                .boardId(dto.getBoardId())
                .id(dto.getId())
                .text(dto.getText())
                .author(dto.getAuthor())
                .memberId(dto.getMemberId())
                .date(dto.getDate())
                .build();
    }
}
