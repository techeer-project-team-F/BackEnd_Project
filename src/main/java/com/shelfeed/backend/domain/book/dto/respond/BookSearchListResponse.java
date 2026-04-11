package com.shelfeed.backend.domain.book.dto.respond;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookSearchListResponse {
    private List<BookSummaryResponse> content;
    private String nextCursor;  // base64 인코딩된 {"page":N}
    private boolean hasNext;
    private int size;
}
