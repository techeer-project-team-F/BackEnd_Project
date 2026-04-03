package com.shelfeed.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthLoginUrlResponse {
    private String loginUrl;
    public static  OAuthLoginUrlResponse of(String loinUrl){
        return OAuthLoginUrlResponse.builder()
                .loginUrl(loinUrl)
                .build();
    }

}
