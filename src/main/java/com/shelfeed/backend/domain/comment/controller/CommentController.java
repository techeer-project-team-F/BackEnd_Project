package com.shelfeed.backend.domain.comment.controller;

import com.shelfeed.backend.domain.comment.dto.request.CommentCreateRequest;
import com.shelfeed.backend.domain.comment.dto.request.CommentUpdateRequest;
import com.shelfeed.backend.domain.comment.dto.response.CommentCreateResponse;
import com.shelfeed.backend.domain.comment.dto.response.CommentListResponse;
import com.shelfeed.backend.domain.comment.dto.response.CommentUpdateResponse;
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
    // 7.2 댓글 목록 조회  GET /api/v1/reviews/{reviewId}/comments
    @GetMapping("/{reviewId}/comments")
    public ApiResponse<CommentListResponse> getComments(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails != null ? userDetails.getMember().getMemberUserId() : null;
        return ApiResponse.success(200,
                commentService.getComments(reviewId, cursor, limit, memberUserId));
    }
    // 7.3 댓글 수정  PUT /api/v1/reviews/{reviewId}/comments/{commentId}
    @PutMapping("/{reviewId}/comments/{commentId}")
    public ApiResponse<CommentUpdateResponse> updateComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentUpdateRequest request) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(200, "댓글이 수정되었습니다.",
                commentService.updateComment(reviewId, commentId, memberUserId, request));
    }
}
