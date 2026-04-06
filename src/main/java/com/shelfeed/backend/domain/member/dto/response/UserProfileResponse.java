package com.shelfeed.backend.domain.member.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.LibraryVisibility;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private LibraryVisibility libraryVisibility;
    private int followerCount;
    private int followingCount;
    private int reviewCount;
    private boolean isFollowing;   // Follow 도메인 구현 후 연결
    private boolean isFollowedBy;  // Follow 도메인 구현 후 연결

    public static UserProfileResponse of(Member member){
        return UserProfileResponse.builder()
                .userId(member.getMemberUserId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .bio(member.getBio())
                .libraryVisibility(member.getLibraryVisibility())
                .followerCount(member.getFollowerCount())
                .followingCount(member.getFollowingCount())
                .reviewCount(member.getReviewCount())
                .isFollowing(false)
                .isFollowedBy(false)
                .build();
    }
}
