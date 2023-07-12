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
@Table
@NoArgsConstructor
@AllArgsConstructor
public class BoardEntity {
    @Id
    // @GeneratedValue(generator = "system-identity")
    // @GenericGenerator(name="system-identity", strategy = GenerationType.IDENTITY)
    @Column(name = "board_idx")
    private String idx;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;

    @Column
    private String image;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String date;
}
