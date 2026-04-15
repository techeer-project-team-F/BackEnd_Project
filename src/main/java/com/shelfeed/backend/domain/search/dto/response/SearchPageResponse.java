package com.shelfeed.backend.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchPageResponse<T> {
    private List<T> content;
    private Long nextCursor;
    private boolean hasNext;
    private int size;

    public static <T> SearchPageResponse<T> empty() {
        return SearchPageResponse.<T>builder()
                .content(List.of())
                .nextCursor(null)
                .hasNext(false)
                .size(0)
                .build();
    }
}
