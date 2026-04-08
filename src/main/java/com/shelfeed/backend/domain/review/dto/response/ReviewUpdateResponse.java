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
public class ReviewUpdateResponse {
    private Long reviewId;
    private byte rating;
    private String content;
    private String quote;
    private Integer readPages;
    private boolean isSpoiler;
    private ReviewVisibility reviewVisibility;
    private ReviewStatus reviewStatus;
    private List<String> tags;
    private LocalDateTime updatedAt;

    public static ReviewUpdateResponse of(Review review, List<String>tags) {
        return ReviewUpdateResponse.builder()
                .reviewId(review.getReviewId())
                .rating(review.getRating())
                .content(review.getContent())
                .quote(review.getQuote())
                .readPages(review.getReadPages())
                .isSpoiler(review.isSpoiler())
                .reviewVisibility(review.getReviewVisibility())
                .reviewStatus(review.getReviewStatus())
                .tags(tags)
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
