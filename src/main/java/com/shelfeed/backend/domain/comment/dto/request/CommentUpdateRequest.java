package com.shelfeed.backend.domain.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentUpdateRequest {

    @NotNull(message = "댓글 내용은 필수입니다.")
    private String content;
}
