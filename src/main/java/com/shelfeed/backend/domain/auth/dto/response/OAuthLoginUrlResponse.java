package com.shelfeed.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthLoginUrlResponse {
    private String loinUrl;
    public static  OAuthLoginUrlResponse of(String loinUrl){
        return OAuthLoginUrlResponse.builder()
                .loinUrl(loinUrl)
                .build();
    }

}
