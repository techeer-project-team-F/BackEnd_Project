package com.shelfeed.backend.domain.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookReviewSearchRequest { //API 명세서 속 쿼리 파라미터 용
    @Schema(defaultValue = "latest", allowableValues = {"latest", "popular", "rating_high", "rating_low"})
    private String sort = "latest";  // latest, popular, rating_high, rating_low

    @Schema(description = "페이지네이션 커서 (선택)", nullable = true)
    private Long cursor;    // 페이지네이션 커서 (선택)

    @Schema(defaultValue = "20", minimum = "1", maximum = "50")
    private int limit = 20; // 기본 20, 최대 50
}
