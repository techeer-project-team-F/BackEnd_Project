package com.shelfeed.backend.domain.report.repository;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByMemberAndReviewId(Member member, Long reviewId);

    boolean existsByMemberAndCommentId(Member member, Long commentId);
}
