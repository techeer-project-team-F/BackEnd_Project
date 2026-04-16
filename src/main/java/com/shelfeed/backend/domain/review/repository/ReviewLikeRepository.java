package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.review.entity.ReviewLike;
import com.shelfeed.backend.domain.review.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike,Long> {

    boolean existsByReview_ReviewIdAndMember_MemberUserId(Long reviewId, Long memberUserId);// 좋아요 중복확인

    Optional<ReviewLike> findByReview_ReviewIdAndMember_MemberUserId(Long reviewId, Long memberUserId);// 좋아요 취소 할 때 삭제 대상 조회용도

    //감상 좋아요 in절
    @Query("""
    SELECT rl.review.reviewId FROM ReviewLike rl 
    WHERE rl.review.reviewId IN :reviewIds AND rl.member.memberUserId = :userId
""")
    Set<Long> findLikedReviewIds(@Param("reviewIds") List<Long> reviewIds,
                                 @Param("userId") Long userId);
}
