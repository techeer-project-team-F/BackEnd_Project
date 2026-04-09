package com.shelfeed.backend.domain.comment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentListResponse {
    private List<CommentResponse> content;
    private Long nextCursor;
    private boolean hasNext;
    private int size;

    public static CommentListResponse of(List<CommentResponse> content, int limit){
        boolean hasNext = content.size() > limit;
        List<CommentResponse> result = hasNext ? content.subList(0, limit) : content;
        Long nextCursor = hasNext ? result.get(result.size() - 1).getCommentId() : null;

        return CommentListResponse.builder()
                .content(result)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(result.size())
                .build();
    }
}
