package com.shelfeed.backend.domain.review.repository;

import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

    List<ReviewTag> findByReview(Review review);

    void deleteByReview(Review review);
}
