package com.shelfeed.backend.domain.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentCreateRequest {
    @NotNull(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long parentCommentId; // null이면 원댓글 값이 있으면 대댓글
}
