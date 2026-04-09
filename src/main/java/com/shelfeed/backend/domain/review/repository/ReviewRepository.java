package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByMemberAndBook_BookIdAndIsDeletedFalse(Member member, Long bookId);//중복 감상 체크

    //내 감상 목록 커서 페이지 네이션
    @Query("SELECT r FROM Review r WHERE r.member = :member AND r.isDeleted = false " +
            "AND (:status IS NULL OR r.reviewStatus = :status) " + // 프론트엔드가 특정 상태를 안 보내면 앞 조건이 참이 되어 전체를 다 보여주고, 특정 값을 보내면 딱 그 상태의 리뷰만 걸러내는 아주 똑똑한 선택적 검색 조건
            "AND (:cursor IS NULL OR r.reviewId < :cursor) " +// 무한 스크롤 커서
            "ORDER BY r.reviewId DESC")// 가장 최근에 쓴 최신 글 순서대로
    List<Review> findMyReviews(@Param("member") Member member,
                               @Param("status") ReviewStatus status,
                               @Param("cursor") Long cursor,
                               Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.member = :member AND r.isDeleted = false " +
            "AND r.reviewVisibility = 'PUBLIC' AND r.reviewStatus = 'PUBLISHED' " +
            "AND (:cursor IS NULL OR r.reviewId < :cursor) " +
            "ORDER BY r.reviewId DESC")
    List<Review> findUserReviews(@Param("member") Member member, @Param("cursor") Long cursor,
                                 Pageable pageable);
    Optional<Review> findByReviewIdAndIsDeletedFalse(Long reviewId);
}
