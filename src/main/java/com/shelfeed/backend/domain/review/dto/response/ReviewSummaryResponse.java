package com.shelfeed.backend.domain.review.dto.response;

import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewSummaryResponse {
    private Long reviewId;
    private BookInfo book;
    private byte rating;
    private String content;
    private String quote;
    private boolean isSpoiler;
    private ReviewVisibility reviewVisibility;
    private ReviewStatus reviewStatus;
    private int likeCount;
    private int commentCount;
    private boolean isLiked;    // Like 도메인 구현 후 연결
    private List<String> tags;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class BookInfo {
        private Long bookId;
        private String title;
        private String author;
        private String coverImageUrl;
    }

    public static ReviewSummaryResponse of(Review review, List<String> tags) {
        return ReviewSummaryResponse.builder()
                .reviewId(review.getReviewId())
                .book(BookInfo.builder()
                        .bookId(review.getBook().getBookId())
                        .title(review.getBook().getTitle())
                        .author(review.getBook().getAuthor())
                        .coverImageUrl(review.getBook().getCoverImageUrl())
                        .build())
                .rating(review.getRating())
                .content(review.getContent())
                .quote(review.getQuote())
                .isSpoiler(review.isSpoiler())
                .reviewVisibility(review.getReviewVisibility())
                .reviewStatus(review.getReviewStatus())
                .likeCount(review.getLikeCount())
                .commentCount(review.getCommentCount())
                .isLiked(false)
                .tags(tags)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
