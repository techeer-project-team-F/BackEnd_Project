package com.shelfeed.backend.domain.follow.service;

import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.follow.dto.response.FollowResponse;
import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shelfeed.backend.domain.follow.entity.QFollow.follow;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final FeedRepository feedRepository;

    //1. 팔로우
    @Transactional
    public FollowResponse follow(Long targetUserId, Long memberUserId){
        //나 자신 팔로우 안됨
        if (targetUserId.equals(memberUserId)){
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }
        Member follower = getMember(memberUserId);
        Member followee = getMember(targetUserId);
        //중복 팔로우 방지
        if (followRepository.existsByFollowerAndFollowee(follower, followee)){
            throw new BusinessException(ErrorCode.ALREADY_FOLLOWING);
        }

        Follow follow = followRepository.save(Follow.create(follower,followee));
        // 카운트 업데이트
        follower.increaseFollowingCount();
        followee.increaseFollowerCount();
        return FollowResponse.of(follow,follower);
    }



    private Member getMember(Long memberUserId){
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
