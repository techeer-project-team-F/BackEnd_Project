package com.shelfeed.backend.domain.feed.service;

import com.shelfeed.backend.domain.feed.dto.response.FeedItemResponse;
import com.shelfeed.backend.domain.feed.dto.response.FeedListResponse;
import com.shelfeed.backend.domain.feed.entity.Feed;
import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.repository.ReviewLikeRepository;
import com.shelfeed.backend.domain.review.repository.ReviewTagRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewTagRepository reviewTagRepository;

    //피드 조회
    public FeedListResponse getFollowingFeed(Long memberUserId, Long cursor, int limit) {
        Member member = getMember(memberUserId);
        //피드페이지
        List<Feed> feeds = feedRepository.findFeed(member,cursor, PageRequest.of(0, limit +1));
        List<FeedItemResponse> content = feeds.stream().map(feed -> {
            Long reviewId = feed.getReview().getReviewId();

            // 내가 감상에 좋아요를 눌렀는 가
            boolean isLiked = reviewLikeRepository.existsByReview_ReviewIdAndMember_MemberUserId(reviewId,memberUserId);

            // 감상에 달린 테그 목록
            List<String> tag = reviewTagRepository.findByReview(feed.getReview()).stream()
                    .map(rt -> rt.getTag().getTagName()).toList();
            return FeedItemResponse.of(feed, isLiked, tag);

        }).toList();
        return FeedListResponse.of(content,limit);
    }

    private Member getMember(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }






}
