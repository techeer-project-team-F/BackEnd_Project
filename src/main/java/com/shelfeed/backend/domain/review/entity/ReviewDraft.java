package com.shelfeed.backend.domain.review.entity;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_drafts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drafts_id")
    private Long draftsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_book_id")
    private LibraryBook libraryBook;

    private Byte rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer readPages;

    private Boolean isSpoiler;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ReviewVisibility reviewVisibility;

    //정적 메서드
    public static ReviewDraft create(Member member, Book book){
        ReviewDraft draft = new ReviewDraft();
        draft.member = member;
        draft.book = book;
        return draft;
    }



}
