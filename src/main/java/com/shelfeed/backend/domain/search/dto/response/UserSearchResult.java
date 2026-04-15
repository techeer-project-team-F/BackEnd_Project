package com.shelfeed.backend.domain.search.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchResult {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private int followerCount;
    private Boolean isFollowing;

    public static UserSearchResult of(Member member, boolean isFollowing){
        return UserSearchResult.builder()
                .userId(member.getMemberUserId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .bio(member.getBio())
                .followerCount(member.getFollowerCount())
                .isFollowing(isFollowing)
                .build();
    }
}
