package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike,Long> {

    boolean existsByReview_ReviewIdAndMember_MemberUserId(Long reviewId, Long memberUserId);// 좋아요 중복확인

    Optional<ReviewLike> findByReview_ReviewIdAndMember_MemberUserId(Long reviewId, Long memberUserId);// 좋아요 취소 할 때 삭제 대상 조회용도

    // 감상 목록 조회 시 내가 좋아요한 감상 ID 배치 조회
    @Query("SELECT rl.review.reviewId FROM ReviewLike rl " +
            "WHERE rl.member.memberUserId = :memberUserId AND rl.review.reviewId IN :reviewIds")
    Set<Long> findLikedReviewIds(@Param("memberUserId") Long memberUserId, @Param("reviewIds") List<Long> reviewIds);
}
