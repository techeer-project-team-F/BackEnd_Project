package com.shelfeed.backend.domain.book.dto.request;

import lombok.Getter;

@Getter
public class BookReviewSearchRequest { //API 명세서 속 쿼리 파라미터 용
    private String sort = "latest";  // latest, popular, rating_high, rating_low
    private Long cursor;    // 페이지네이션 커서 (선택)
    private int limit = 20; // 기본 20, 최대 50
}
