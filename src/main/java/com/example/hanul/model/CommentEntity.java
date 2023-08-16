package com.example.hanul.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Builder
@Data
@Table(name = "Comment")
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(name = "comment_id")
    private String id;
    @Column(nullable = false)
    private String boardId;
    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private String author;
    @Column(nullable = false)
    private String memberId;
    @Column(nullable = false)
    private String date;
}
