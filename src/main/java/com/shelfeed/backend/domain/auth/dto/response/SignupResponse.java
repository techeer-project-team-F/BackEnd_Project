package com.shelfeed.backend.domain.auth.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {

    private String accessToken;
    private long accessTokenExpiresIn; // 초 단위 (예: 1800)
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long userId;
        private String email;
        private String nickname;
        private boolean emailVerified;
    }

    public static SignupResponse of(Member member, String accessToken, long accessTokenExpiresIn) {
        return SignupResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .user(UserInfo.builder()
                        .userId(member.getMemberUserId())
                        .email(member.getEmail())
                        .nickname(member.getNickname())
                        .emailVerified(member.isEmailVerified())
                        .build())
                .build();
    }
}
