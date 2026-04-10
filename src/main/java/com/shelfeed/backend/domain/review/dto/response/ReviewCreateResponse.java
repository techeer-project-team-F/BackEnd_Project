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
public class ReviewCreateResponse {
    private Long reviewId;
    private Long bookId;
    private byte rating;
    private String content;
    private String quote;
    private Integer readPages;
    private Boolean isSpoiler;
    private ReviewVisibility reviewVisibility;
    private ReviewStatus reviewStatus;
    private int likeCount;
    private int commentCount;
    private List<String> tags;
    private LocalDateTime createdAt;

    public static ReviewCreateResponse of(Review review, List<String> tags) {//나중에 태그 기능 넣을 거라
        return ReviewCreateResponse.builder()
                .reviewId(review.getReviewId())
                .bookId(review.getBook().getBookId())
                .rating(review.getRating())
                .content(review.getContent())
                .quote(review.getQuote())
                .readPages(review.getReadPages())
                .isSpoiler(review.isSpoiler())
                .reviewVisibility(review.getReviewVisibility())
                .reviewStatus(review.getReviewStatus())
                .likeCount(review.getLikeCount())
                .commentCount(review.getCommentCount())
                .tags(tags)
                .createdAt(review.getCreatedAt())
                .build();
    }

}
