package com.shelfeed.backend.domain.review.dto.response;

import com.shelfeed.backend.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewLikeResponse {
    private Long reviewId;
    private int likeCount;

    public static ReviewLikeResponse of(Review review){
        return ReviewLikeResponse.builder()
                .reviewId(review.getReviewId())
                .likeCount(review.getLikeCount())
                .build();
    }
}
