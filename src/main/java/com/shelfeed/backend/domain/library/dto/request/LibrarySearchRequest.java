package com.shelfeed.backend.domain.library.dto.request;

import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import lombok.Getter;

@Getter
public class LibrarySearchRequest {
    private ReadingStatus status;   // 독서 상태 필터 (선택)
    private Long cursor;            // 페이지네이션 커서 (선택)
    private int limit = 20;

}
