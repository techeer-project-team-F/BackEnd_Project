package com.shelfeed.backend.domain.follow.service;

import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.follow.dto.response.FollowListResponse;
import com.shelfeed.backend.domain.follow.dto.response.FollowMemberResponse;
import com.shelfeed.backend.domain.follow.dto.response.FollowResponse;
import com.shelfeed.backend.domain.follow.dto.response.UnfollowResponse;
import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    //2.언팔로우
    @Transactional
    public UnfollowResponse unfollow(Long targetUserId, Long memberUserId){
        Member follower = getMember(memberUserId);
        Member followee = getMember(targetUserId);
        //삭제 대상 조회
        Follow follow = followRepository.findByFollowerAndFollowee(follower,followee)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));
        followRepository.delete(follow);
        // 카운트 업데이트
        follower.decreaseFollowingCount();
        followee.decreaseFollowerCount();
        //엔팔한 멤버의 피드 내 피드화면에서 제거
        feedRepository.deleteByMemberAndReview_Member(follower,followee);

        return UnfollowResponse.of(followee,follower);
    }
    //3.팔로워 목록
    public FollowListResponse getFollowers(Long targetUserId, Long cursor, int limit, Long memberUserId){
        Member target = getMember(targetUserId);
        //사용자 정보 1번만 조회
        Member me = (memberUserId != null) ? getMember(memberUserId) : null;
        //팔로워 목록 조회(패치조인)
        List<Follow> follows = followRepository.findFollowersWithMember(target, cursor, PageRequest.of(0, limit + 1));
        // 대상이 되는 팔러워 리스트 추출
        List<Member> candidates = follows.stream().map(Follow::getFollower).toList();
        //팔로우 관계 in 절
        Set<Long> followingIds = Collections.emptySet();
        Set<Long> followedByIds = Collections.emptySet();

        if (me != null && !candidates.isEmpty()) {
            followingIds = followRepository.findFollowingIds(me, candidates);
            followedByIds = followRepository.findFollowedByIds(me, candidates);
        }

        final Set<Long> finalFollowingIds = followingIds;
        final Set<Long> finalFollowedByIds = followedByIds;


        //팔로워 목록에 표기할 유저
        List<FollowMemberResponse> content = follows.stream().map(follow ->{
            Member follower = follow.getFollower();
            Long followerId = follower.getMemberUserId();

            //현재 팔로우 중인지
            boolean isFollowing = finalFollowingIds.contains(followerId);
            //타 유저가 나를 팔로우 중인지
            boolean isFollowBy = finalFollowedByIds.contains(followerId);

            return FollowMemberResponse.of(follower, isFollowing, isFollowBy);
        }).toList();

        return  FollowListResponse.of(content,limit);
    }

    //4.팔로잉 목록
    public FollowListResponse getFollowings(Long targetUserId, Long cursor, int limit, Long memberUserId){
        Member target = getMember(targetUserId);
        //팔로워 페이지
        List<Follow> follows = followRepository.findFollowings(target, cursor, PageRequest.of(0, limit + 1));
        //팔로워 목록에 표기할 유저
        List<FollowMemberResponse> content = follows.stream().map(follow ->{
            Member followee = follow.getFollowee();
            //현재 팔로우 중인지
            boolean isFollowing = memberUserId != null && followRepository.existsByFollowerAndFollowee(getMember(memberUserId), followee);
            //타 유저가 나를 팔로우 중인지
            boolean isFollowBy = memberUserId != null && followRepository.existsByFollowerAndFollowee(followee,getMember(memberUserId));

            return FollowMemberResponse.of(followee, isFollowing, isFollowBy);
        }).toList();

        return  FollowListResponse.of(content,limit);
    }



    private Member getMember(Long memberUserId){
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
