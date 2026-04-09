package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike,Long> {

    boolean existsByReview_ReviewIdAndMember_MemberUserId(Long reviewId, Long memberUserId);// 좋아요 중복확인

    Optional<ReviewLike> findByReview_ReviewIdAndMember_MemberUserId(Long reviewId, Long memberUserId);// 좋아요 취소 할 때 삭제 대상 조회용도
}
