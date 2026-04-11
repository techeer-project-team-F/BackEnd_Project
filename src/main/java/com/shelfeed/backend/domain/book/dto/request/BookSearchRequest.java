package com.shelfeed.backend.domain.book.dto.request;

import lombok.Getter;

@Getter
public class BookSearchRequest { //API 명세서 속 쿼리 파라미터 용
    private String query;    // 검색어 (필수)
    private String cursor;   // 페이지네이션 커서 base64 인코딩 (선택)
    private int limit = 20;  // 기본 20, 최대 50

}
