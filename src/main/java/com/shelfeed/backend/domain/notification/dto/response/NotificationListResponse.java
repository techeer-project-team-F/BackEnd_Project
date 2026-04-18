package com.shelfeed.backend.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private List<NotificationItemResponse> content;
    private String nextCursor;
    private boolean hasNext;
    private int size;

    public static NotificationListResponse of(List<NotificationItemResponse> content,
                                              int limit,
                                              String nextCursor) {
        boolean hasNext = content.size() > limit;
        List<NotificationItemResponse> result = hasNext ? content.subList(0, limit) : content;

        return NotificationListResponse.builder()
                .content(result)
                .nextCursor(hasNext ? nextCursor : null)
                .hasNext(hasNext)
                .size(limit) // 요청된 페이지 크기
                .build();
    }
}

