package com.shelfeed.backend.domain.follow.controller;

import com.shelfeed.backend.domain.follow.dto.response.FollowResponse;
import com.shelfeed.backend.domain.follow.dto.response.UnfollowResponse;
import com.shelfeed.backend.domain.follow.service.FollowService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 11.1 팔로우  POST /api/v1/users/{userId}/follow
    @PostMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FollowResponse> follow(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(201, "팔로우했습니다.",
                followService.follow(userId, memberUserId));
    }

    // 11.2 언팔로우  DELETE /api/v1/users/{userId}/follow
    @DeleteMapping("/{userId}/follow")
    public ApiResponse<UnfollowResponse> unfollow(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(200, "언팔로우했습니다.",
                followService.unfollow(userId, memberUserId));
    }
}
