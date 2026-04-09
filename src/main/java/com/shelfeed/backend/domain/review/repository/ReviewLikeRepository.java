package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike,Long> {

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);// 좋아요 중복확인

    Optional<ReviewLike> findByReviewIdAndMemberId(Long reviewId, Long memberId);// 좋아요 취소 할 때 삭제 대상 조회용도
}
