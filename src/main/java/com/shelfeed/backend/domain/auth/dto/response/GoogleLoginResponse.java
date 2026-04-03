package com.shelfeed.backend.domain.auth.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleLoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
    private boolean isNewUser;
    private UserInfo user;

    @Getter
    @Builder
    public static class  UserInfo{
        private Long userId;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private boolean onboardingCompleted;
    }

    public static GoogleLoginResponse of(Member member, String accessToken, String refreshToken, Long accessTokenExpiresIn, Long refreshTokenExpiresIn, boolean isNewUser){
        return GoogleLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .isNewUser(isNewUser)
                .user(UserInfo.builder()
                        .userId(member.getMemberUserId())
                        .email(member.getEmail())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImageUrl())
                        .onboardingCompleted(member.isOnboardingCompleted())
                        .build())
                .build();
    }


}
