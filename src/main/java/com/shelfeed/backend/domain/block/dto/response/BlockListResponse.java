package com.shelfeed.backend.domain.block.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BlockListResponse {
    private List<BlockMemberResponse> content;
    private Long nextCursor;
    private boolean hasNext;
    private int size;

    public static BlockListResponse of(List<BlockMemberResponse> content, int limit){
        boolean hasNext = content.size() > limit;
        List<BlockMemberResponse> result = hasNext ? content.subList(0, limit) : content;
        Long nextCursor = hasNext ? result.get(result.size() - 1).getUserId() : null;
        return BlockListResponse.builder()
                .content(result)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(result.size())
                .build();
    }
}
