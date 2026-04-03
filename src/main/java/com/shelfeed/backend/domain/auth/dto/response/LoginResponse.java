package com.shelfeed.backend.domain.auth.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private Long accessTokenExpiresIn;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo{
        private Long userId;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private boolean emailVerified;
        private boolean onboardingCompleted;
    }

    public static LoginResponse of(Member member, String accessToken, Long expiresIn) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(expiresIn)
                .user(UserInfo.builder()
                        .userId(member.getMemberUserId())
                        .email(member.getEmail())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImageUrl())
                        .emailVerified(member.isEmailVerified())
                        .onboardingCompleted(member.isOnboardingCompleted())
                        .build())
                .build();
    }
}
