package com.shelfeed.backend.domain.follow.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FollowListResponse {
    private List<FollowMemberResponse> content;
    private Long nextCursor;
    private boolean hasNext;
    private int size;

    public static FollowListResponse of(List<FollowMemberResponse> content, int limit) {
        boolean hasNext = content.size() > limit;
        List<FollowMemberResponse> result = hasNext ? content.subList(0, limit) : content;
        Long nextCursor = hasNext ? result.get(result.size() - 1).getUserId() : null;

        return FollowListResponse.builder()
                .content(result)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(result.size())
                .build();
    }
}
