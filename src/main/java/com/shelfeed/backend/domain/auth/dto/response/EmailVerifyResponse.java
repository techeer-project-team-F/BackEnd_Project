package com.shelfeed.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifyResponse {
    private String email;
    private boolean emailVerified;

    public static EmailVerifyResponse of(String email, boolean emailVerified){
        return EmailVerifyResponse.builder()
                .email(email)
                .emailVerified(emailVerified)
                .build();
    }

}
