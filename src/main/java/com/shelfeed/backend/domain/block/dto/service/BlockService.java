package com.shelfeed.backend.domain.block.dto.service;

import com.shelfeed.backend.domain.block.entity.Block;
import com.shelfeed.backend.domain.block.repository.BlockRepository;
import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final MemberRepository memberRepository;
    private final BlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final FeedRepository feedRepository;

    //차단
    @Transactional
    public void block (Long targetUserId, Long memberUserId){
        //본인 차단 못함
        if (targetUserId.equals(memberUserId)) {
            throw new BusinessException(ErrorCode.ALREADY_BLOCKED);
        }

        Member blocker = getMember(memberUserId);
        Member blocked = getMember(targetUserId);
        //중복차단 방지
        if (blockRepository.existsByBlockerAndBlocked(blocker, blocked)){
            throw new BusinessException(ErrorCode.ALREADY_BLOCKED);
        }
        blockRepository.save(Block.create(blocker, blocked));

        removeFollowIfExists(blocker,blocked);// 내가 상대방 해제
        removeFollowIfExists(blocked, blocker);// 상대방이 날 헤제
        //상대방 피드 제거
        feedRepository.deleteByMemberAndReview_Member(blocker,blocked);
    }

    //멤버 찾기
    private Member getMember(Long memberUserId){
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    //팔로우 관계가 있으면 카운트 해제
    private void removeFollowIfExists(Member follower, Member followee) {
        Optional<Follow> follow = followRepository.findByFollowerAndFollowee(follower, followee);
        if (follow.isPresent()){
            followRepository.delete(follow.get());
            follower.decreaseFollowingCount();
            followee.decreaseFollowerCount();
        }
    }
//차단 해제
    @Transactional
    public void unblock(Long targetUserId, Long memberUserId) {
        Member blocker = getMember(memberUserId);
        Member blocked = getMember(targetUserId);

        Block block = blockRepository.findByBlockerAndBlocked(blocker,blocked)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCK_NOT_FOUND));

        blockRepository.delete(block);
    }




}
