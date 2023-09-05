package com.example.hanul.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@Table(name = "Item")
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntity {
    @Id
    @Column(name = "item_id")
    private String id;

    @Column(nullable = true)
    private String itemNm;

    @Column(nullable = true, length = 2000) // 길이를 2000으로 변경
    private String itemDetail;

    @Column(nullable = true)
    private String posterUrl; // 영화 포스터 이미지 URL

    @Column(nullable = true)
    private String genreName;

    @Column(nullable = true)
    private String director;

    @Column(nullable = true)
    private String cast;

    @Column(nullable = true, length = 1000)
    private String keyword;

    @ManyToMany(mappedBy = "bookmarkedItems") // "bookmarkedItems"는 MemberEntity에서 정의한 필드 이름
    @JsonIgnore
    private List<MemberEntity> bookmarkedByMembers;
}
