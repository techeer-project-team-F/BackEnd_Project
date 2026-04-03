package com.shelfeed.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LogoutRequest {
    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshToken;
}
