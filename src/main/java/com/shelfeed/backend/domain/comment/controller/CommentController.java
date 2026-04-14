package com.shelfeed.backend.domain.comment.controller;

import com.shelfeed.backend.domain.comment.dto.request.CommentCreateRequest;
import com.shelfeed.backend.domain.comment.dto.response.CommentCreateResponse;
import com.shelfeed.backend.domain.comment.service.CommentService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 7.1 댓글 작성  POST /api/v1/reviews/{reviewId}/comments
    @PostMapping("/{reviewId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentCreateResponse> createComment(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentCreateRequest request) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(201, "댓글이 등록되었습니다.",
                commentService.createComment(reviewId, memberUserId, request));
    }

}
