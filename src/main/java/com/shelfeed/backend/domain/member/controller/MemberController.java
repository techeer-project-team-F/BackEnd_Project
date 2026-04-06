package com.shelfeed.backend.domain.member.controller;

import com.shelfeed.backend.domain.member.dto.response.MyProfileResponse;
import com.shelfeed.backend.domain.member.service.MemberService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    // 내 프로필 조회  GET /api/v1/users/me
    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(200,
                memberService.getMyProfile(userDetails.getMember().getMemberUserId()));
    }
}
