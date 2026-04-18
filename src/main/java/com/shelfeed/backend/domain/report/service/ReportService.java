package com.shelfeed.backend.domain.report.service;

import com.shelfeed.backend.domain.comment.entity.Comment;
import com.shelfeed.backend.domain.comment.repository.CommentRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.report.dto.request.CreateReportRequest;
import com.shelfeed.backend.domain.report.dto.response.CreateReportResponse;
import com.shelfeed.backend.domain.report.entity.Report;
import com.shelfeed.backend.domain.report.enums.ReportTargetType;
import com.shelfeed.backend.domain.report.repository.ReportRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    public CreateReportResponse createReport(Long memberUserId, CreateReportRequest request) {
        Member reporter = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ReportTargetType targetType = request.getTargetType();
        Long targetId = request.getTargetId();

        Report report = switch (targetType) {
            case REVIEW -> createReviewReport(reporter, memberUserId, request);
            case COMMENT -> createCommentReport(reporter, memberUserId, request);
        };

        Report saved = reportRepository.save(report);
        return CreateReportResponse.of(saved, targetType, targetId);
    }

    private Report createReviewReport(Member reporter, Long memberUserId, CreateReportRequest request) {
        Long reviewId = request.getTargetId();

        if (reportRepository.existsByMemberAndReviewId(reporter, reviewId)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Review review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND));

        if (review.getMember().getMemberUserId().equals(memberUserId)) {
            throw new BusinessException(ErrorCode.REPORT_SELF_NOT_ALLOWED);
        }

        return Report.createReviewReport(reporter, reviewId, request.getReason(), request.getDescription());
    }

    private Report createCommentReport(Member reporter, Long memberUserId, CreateReportRequest request) {
        Long commentId = request.getTargetId();

        if (reportRepository.existsByMemberAndCommentId(reporter, commentId)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_TARGET_NOT_FOUND));

        if (comment.getMember().getMemberUserId().equals(memberUserId)) {
            throw new BusinessException(ErrorCode.REPORT_SELF_NOT_ALLOWED);
        }

        return Report.createCommentReport(reporter, commentId, request.getReason(), request.getDescription());
    }
}

