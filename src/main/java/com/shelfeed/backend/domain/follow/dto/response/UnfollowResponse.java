package com.shelfeed.backend.domain.follow.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnfollowResponse {
    private int followerCount;
    private int followingCount;

    public static UnfollowResponse of(Member followee, Member member) {
        return UnfollowResponse.builder()
                .followerCount(followee.getFollowerCount())
                .followingCount(member.getFollowingCount())
                .build();
    }
}
