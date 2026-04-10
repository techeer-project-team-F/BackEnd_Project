package com.shelfeed.backend.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleLoginResponse {
    private String accessToken;
    private Long accessTokenExpiresIn;
    @JsonProperty("isNewUser")
    private boolean isNewUser;
    private UserInfo user;

    @Getter
    @Builder
    public static class  UserInfo{
        private Long userId;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private boolean emailVerified;
        private boolean onboardingCompleted;
    }

    public static GoogleLoginResponse of(Member member, String accessToken,
                                         long accessTokenExpiresIn, boolean isNewUser) {
        return GoogleLoginResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .isNewUser(isNewUser)
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
