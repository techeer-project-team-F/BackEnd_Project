package com.shelfeed.backend.domain.member.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.LibraryVisibility;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private LibraryVisibility libraryVisibility;
    private boolean emailVerified;
    private boolean onboardingCompleted;
    private int followerCount;
    private int followingCount;
    private int reviewCount;

    public static MyProfileResponse of(Member member){
        return MyProfileResponse.builder()
                .userId(member.getMemberUserId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .bio(member.getBio())
                .libraryVisibility(member.getLibraryVisibility())
                .emailVerified(member.isEmailVerified())
                .onboardingCompleted(member.isOnboardingCompleted())
                .followerCount(member.getFollowerCount())
                .followingCount(member.getFollowingCount())
                .reviewCount(member.getReviewCount())
                .build();
    }
}
