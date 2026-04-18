package com.shelfeed.backend.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {
    private long unreadCount;

    public static UnreadCountResponse of(long unreadCount) {
        return UnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }
}

