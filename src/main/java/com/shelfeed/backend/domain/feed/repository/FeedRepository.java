package com.shelfeed.backend.domain.feed.repository;

import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedRepository extends JpaRepository {
    //팔로잉 목록 페이지네이션
    @Query("""
            SELECT f FROM Follow f
            WHERE f.followee = :member
            AND (:cursor IS NULL OR f.followId< :cursor) ORDER BY f.followId DESC
""")
    List<Follow> findFollowings(@Param("member") Member member, @Param("cursor") Long cursor,
                                Pageable pageable);

    //언팔로우 한 사용자 감상 피드에서 제거
    void deleteByMemberAndReview_Member(Member follower, Member followee);

    // 감상 삭제 시 피드에서 제거
    void deleteByReview(Review review);
}
