package com.shelfeed.backend.domain.member.controller;

import com.shelfeed.backend.domain.member.dto.request.UpdateProfileRequest;
import com.shelfeed.backend.domain.member.dto.response.MyProfileResponse;
import com.shelfeed.backend.domain.member.dto.response.UpdateProfileResponse;
import com.shelfeed.backend.domain.member.dto.response.UserProfileResponse;
import com.shelfeed.backend.domain.member.service.MemberService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    // 2. 내 프로필 조회  GET /api/v1/users/me
    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(200,
                memberService.getMyProfile(userDetails.getMember().getMemberUserId()));
    }

    // 3. 프로필 수정  PATCH /api/v1/users/me
    @PostMapping("/me")
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request){
        return ApiResponse.success(200, "프로필이 수정되었습니다.", memberService.updateProfile(userDetails.getMember().getMemberUserId(),request));
    }

    // 4. 타 유저 프로필 조회  GET /api/v1/users/{userId}
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long userId){
        return ApiResponse.success(200, memberService.getUserProfile(userId));
    }

}
