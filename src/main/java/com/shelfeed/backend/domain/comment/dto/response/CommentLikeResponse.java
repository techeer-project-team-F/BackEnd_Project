package com.shelfeed.backend.domain.comment.dto.response;

import com.shelfeed.backend.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentLikeResponse {

    private Long commentId;
    private int likeCount;

    public static CommentLikeResponse of(Comment comment) {
        return CommentLikeResponse.builder()
                .commentId(comment.getCommentId())
                .likeCount(comment.getLikeCount())
                .build();
    }
}
