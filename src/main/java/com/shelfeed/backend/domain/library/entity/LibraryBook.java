package com.shelfeed.backend.domain.library.entity;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "library_books", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id","book_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LibraryBook extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_book_id")
    private Long libraryBookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingStatus status;

    private LocalDate startedAt;
    private LocalDate finishedAt;

    public static LibraryBook create(Member member, Book book, ReadingStatus status){
        LibraryBook libraryBook = new LibraryBook();
        libraryBook.memberId = member;
        libraryBook.book = book;
        libraryBook.status =status;
        return libraryBook;
    }
    public void updateStatus(ReadingStatus status){
        this.status = status;
    }

}
