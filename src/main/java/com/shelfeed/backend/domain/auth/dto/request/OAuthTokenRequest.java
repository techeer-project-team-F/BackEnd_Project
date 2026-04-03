package com.shelfeed.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthTokenRequest {
    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String code;

    @NotBlank(message = "redirectUri를 입력해주세요.")
    private String redirectUri;
}
