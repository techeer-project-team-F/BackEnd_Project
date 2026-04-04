package com.shelfeed.backend.domain.auth.controller;

import com.shelfeed.backend.domain.auth.dto.request.SignupRequest;
import com.shelfeed.backend.domain.auth.dto.response.SignupResponse;
import com.shelfeed.backend.domain.auth.service.AuthService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    // 1. 이메일 회원가입  POST /api/v1/auth/signup
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)//성공 시 상태
    public ApiResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request,HttpServletResponse response) {// 유효성 검사 + JSON 형태의 요청 본문 DTO로 변환
                AuthService.TokenPair result = authService.signup(request);
                setRefreshTokenCookie(response, result.refreshToken());
                    return ApiResponse.success(201,"회원가입이 완료되었습니다. 이메일 인증을 진행해주세요.", result.response());
    }

    // 2. 이메일 인증코드 확인  POST /api/v1/auth/email/verify














    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken){//HttpServletResponse 응답 메시지를 담는 객체
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)//ResponseCookie : 쿠키 설정 빌더 객체, from : 쿠키의 이름과 그 안에 담길 값설정
                .httpOnly(true)//XSS 공격 방어
                .secure(true)// HTTPS 통신일 때만 쿠키를 전송
                .sameSite("Strict")// CSRF 공격 방어(현재 사이트와 쿠키를 요청하는 사이트가 같을 때만 전송)
                .path("/api/v1/auth")// 쿠키가 전송될 경로 제한
                .maxAge(Duration.ofSeconds(jwtProvider.getRefreshTokenExpiresIn()))//유효시간
                .build();//객체 생성
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }







}
