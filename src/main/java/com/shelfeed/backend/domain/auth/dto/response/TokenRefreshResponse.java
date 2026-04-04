package com.shelfeed.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private Long accessTokenExpiresIn;

    public static TokenRefreshResponse of(String accessToken, long accessTokenExpiresIn){
        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }
}
