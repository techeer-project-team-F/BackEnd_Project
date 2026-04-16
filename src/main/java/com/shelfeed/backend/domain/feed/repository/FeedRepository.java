package com.shelfeed.backend.domain.feed.repository;

import com.shelfeed.backend.domain.feed.entity.Feed;
import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.entity.ReviewTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    // 피드 목록 커서 페이지네이션
    @Query("""
            SELECT f FROM Feed f
            WHERE f.member = :member
            AND (:cursor IS NULL OR f.feedId < :cursor) ORDER BY f.feedId DESC
""")
    List<Feed> findFeed(@Param("member") Member member, @Param("cursor") Long cursor,
                        Pageable pageable);
    //패치조인 쿼리 추가
    @Query("""
    SELECT f FROM Feed f JOIN FETCH f.review r JOIN FETCH r.member JOIN FETCH r.book
    WHERE f.member = :member
    AND (:cursor IS NULL OR f.feedId < :cursor)
    ORDER BY f.feedId DESC
""")List<Feed> findFeedWithDetails(@Param("member") Member member,
                                   @Param("cursor") Long cursor,
                                   Pageable pageable);


    //언팔로우 한 사용자 감상 피드에서 제거
    void deleteByMemberAndReview_Member(Member follower, Member followee);

    // 감상 삭제 시 피드에서 제거
    void deleteByReview(Review review);
}
