package com.shelfeed.backend.domain.follow.controller;

import com.shelfeed.backend.domain.block.dto.service.BlockService;
import com.shelfeed.backend.domain.follow.dto.response.FollowListResponse;
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
    private final BlockService blockService;

    // 1 팔로우  POST /api/v1/users/{userId}/follow
    @PostMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FollowResponse> follow(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(201, "팔로우했습니다.", followService.follow(userId, memberUserId));
    }

    // 2 언팔로우  DELETE /api/v1/users/{userId}/follow
    @DeleteMapping("/{userId}/follow")
    public ApiResponse<UnfollowResponse> unfollow(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(200, "언팔로우했습니다.", followService.unfollow(userId, memberUserId));
    }

    // 3 팔로워 목록  GET /api/v1/users/{userId}/followers
    @GetMapping("/{userId}/followers")
    public ApiResponse<FollowListResponse> getFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails != null ? userDetails.getMember().getMemberUserId() : null;
        return ApiResponse.success(200, followService.getFollowers(userId, cursor, limit, memberUserId));
    }

    // 4 팔로잉 목록  GET /api/v1/users/{userId}/following
    @GetMapping("/{userId}/following")
    public ApiResponse<FollowListResponse> getFollowings(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails != null ? userDetails.getMember().getMemberUserId() : null;
        return ApiResponse.success(200, followService.getFollowings(userId, cursor, limit, memberUserId));
    }
    // 5 사용자 차단  POST /api/v1/users/{userId}/block
    @PostMapping("/{userId}/block")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        blockService.block(userId, memberUserId);
        return ApiResponse.success(201, "사용자를 차단했습니다.");
    }
}
