package com.shelfeed.backend.domain.review.entity;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import com.shelfeed.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_book_id")
    private LibraryBook LibraryBook;

    @Column(nullable = false)
    private byte rating;

    @Column(columnDefinition = "Text", nullable = false)
    private String content;

    private Integer readPages;

    @Column(nullable = false)
    private boolean isSpoiler;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReviewVisibility reviewVisibility;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;





}
