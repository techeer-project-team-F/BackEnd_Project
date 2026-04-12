package com.shelfeed.backend.domain.tag.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long tagId;

    @Column(length = 100)
    private String tagName;
    //태그 이름 넣으면 그 이름을 가진 태그를 생성함
    public static Tag create(String tagName) {
        Tag tag = new Tag();
        tag.tagName = tagName;
        return tag;
    }
}
