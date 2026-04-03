package com.shelfeed.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequest {
    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String code;

    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshToken;
}
