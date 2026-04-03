package com.shelfeed.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;

    public static TokenRefreshResponse of(String accessToken, String refreshToken, long accessTokenExpiresIn, long refreshTokenExpiresIn){
        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
