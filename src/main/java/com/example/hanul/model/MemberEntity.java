package com.example.hanul.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "member_id")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String profilePictureName; // 프로필 사진 URL을 저장하는 필드

    @ManyToMany
    @JoinTable(
            name = "member_item", // 연결 테이블의 이름
            joinColumns = @JoinColumn(name = "member_id"), // MemberEntity의 외래 키
            inverseJoinColumns = @JoinColumn(name = "item_id") // ItemEntity의 외래 키
    )
    @JsonIgnore
    private List<ItemEntity> bookmarkedItems;

}
