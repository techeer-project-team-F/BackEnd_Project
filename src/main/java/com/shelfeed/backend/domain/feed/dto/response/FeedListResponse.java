package com.shelfeed.backend.domain.feed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FeedListResponse {
    private List<FeedItemResponse> content;
    private Long nextCursor;
    private boolean hasNext;
    private int size;

    public static FeedListResponse of(List<FeedItemResponse> content, int limit) {
        //페이지네이션
        boolean hasNext = content.size() > limit;
        List<FeedItemResponse> result = hasNext ? content.subList(0, limit) : content;
        Long nextCursor = hasNext ? result.get(result.size() - 1).getFeedId() : null;

        return FeedListResponse.builder()
                .content(result)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(result.size())
                .build();
    }

}
