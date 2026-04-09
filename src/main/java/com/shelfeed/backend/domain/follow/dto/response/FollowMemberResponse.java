package com.shelfeed.backend.domain.follow.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowMemberResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private Boolean isFollowing; // 내가 이 사람을 팔로우하냐
    private Boolean isFollowedBy;// 이 사람이 나를 팔로우하냐

    public static FollowMemberResponse of(Member target, boolean isFollowing, boolean isFollowedBy) {
        return FollowMemberResponse.builder()
                .userId(target.getMemberUserId())
                .nickname(target.getNickname())
                .profileImageUrl(target.getProfileImageUrl())
                .bio(target.getBio())
                .isFollowing(isFollowing)
                .isFollowedBy(isFollowedBy)
                .build();
    }
}
