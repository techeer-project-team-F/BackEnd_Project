package com.shelfeed.backend.domain.search.dto.response;

import com.shelfeed.backend.domain.book.entity.Book;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookSearchResult {
    private Long bookId;
    private String isbn13;
    private String title;
    private String author;
    private String coverImageUrl;
    private Double averageRating;
    private Long reviewCount;

    public static BookSearchResult of(Book book, Double averageRating, Long reviewCount) {
        return BookSearchResult.builder()
                .bookId(book.getBookId())
                .isbn13(book.getIsbn13())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImageUrl(book.getCoverImageUrl())
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : null)
                .reviewCount(reviewCount)
                .build();
    }
}
