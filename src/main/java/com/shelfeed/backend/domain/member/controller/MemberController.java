package com.shelfeed.backend.domain.member.controller;

import com.shelfeed.backend.domain.member.dto.request.ChangePasswordRequest;
import com.shelfeed.backend.domain.member.dto.request.UpdateProfileRequest;
import com.shelfeed.backend.domain.member.dto.response.MyProfileResponse;
import com.shelfeed.backend.domain.member.dto.response.UpdateProfileResponse;
import com.shelfeed.backend.domain.member.dto.response.UserProfileResponse;
import com.shelfeed.backend.domain.member.service.MemberService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.jwt.JwtProvider;
import com.shelfeed.backend.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
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

    // 5. 비밀번호 변경  PUT /api/v1/users/me/password
    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request, HttpServletResponse response){//HttpServletResponse response :HTTP 응답 조작 가능하게
            MemberService.NewTokenPair result = memberService.changePassword(userDetails.getMember().getMemberUserId(), request);
        setRefreshTokenCookie(response, result.refreshToken());

        return ApiResponse.success(200,"비밀번호가 변경되었습니다.");
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)// XSS 보안
                .secure(true)//HTTPS 통신만 허용
                .sameSite("Strict")//CSRF 차단
                .path("/api/v1/auth")//인증 주소로 요청 보낼 때만 쿠키 따라기도록
                .maxAge(Duration.ofSeconds(jwtProvider.getRefreshTokenExpiresIn()))//토큰 유효기간
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
