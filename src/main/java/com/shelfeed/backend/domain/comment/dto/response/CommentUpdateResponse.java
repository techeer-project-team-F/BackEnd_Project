package com.shelfeed.backend.domain.comment.dto.response;

import com.shelfeed.backend.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentUpdateResponse {

    private Long commentId;
    private String content;
    private LocalDateTime updatedAt;

    public static CommentUpdateResponse of(Comment comment) {
        return CommentUpdateResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
