package com.shelfeed.backend.domain.comment.service;

import com.shelfeed.backend.domain.comment.dto.request.CommentCreateRequest;
import com.shelfeed.backend.domain.comment.dto.response.CommentCreateResponse;
import com.shelfeed.backend.domain.comment.entity.Comment;
import com.shelfeed.backend.domain.comment.repository.CommentLikeRepository;
import com.shelfeed.backend.domain.comment.repository.CommentRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    //1. 댓글 작성
    @Transactional
    public CommentCreateResponse createComment(Long revireId, Long memberUserId, CommentCreateRequest request){
        Member member = getMember(memberUserId);
        Review review = getReview(revireId);
        Comment comment;
        if (request.getParentCommentId() == null) {//새 댓글이면 새 댓글 생성
            comment = Comment.createOriginComment(review, member, request.getContent());
        }
        else {// 댓글이면 부모 댓글에 생성 그것도 아니면 예외
            Comment parentComment = commentRepository.findByCommentIdAndIsDeletedFalse(request.getParentCommentId())
                    .orElseThrow(()-> new BusinessException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
            if (parentComment.getParentComment() != null){ //부모 댓글이 이미 답글 이면 예외
                throw new BusinessException(ErrorCode.NESTED_REPLY_NOT_ALLOWED);
            }
            comment = Comment.createReply(review,member,parentComment,request.getContent());// 대댓글 작성
        }
        commentRepository.save(comment);
        review.increaseCommentCount();
        return CommentCreateResponse.of(comment);
    }


    //추가 메서드
    private Member getMember(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
