package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

    List<ReviewTag> findByReview(Review review);

    void deleteByReview(Review review);

    //감상 태그 in절
    @Query("""
    SELECT rt FROM ReviewTag rt JOIN FETCH rt.tag
    WHERE  rt.review.reviewId IN :reviewIds
""")List<ReviewTag> findByReviewIdIn(@Param("reviewIds") List<Long> reviewIds);
}
