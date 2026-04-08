package com.shelfeed.backend.domain.review.dto.request;

import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import lombok.Getter;

@Getter
public class ReviewSearchRequest {
    private ReviewStatus status; // 상태 필터 (DRAFT, PUBLISHED)
    private Long cursor; //페이지네이션
    private int limit = 20; //한 방에 20개 가져오기
}
