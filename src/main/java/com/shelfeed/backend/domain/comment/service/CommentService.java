package com.shelfeed.backend.domain.comment.service;

import com.shelfeed.backend.domain.comment.dto.request.CommentCreateRequest;
import com.shelfeed.backend.domain.comment.dto.request.CommentUpdateRequest;
import com.shelfeed.backend.domain.comment.dto.response.*;
import com.shelfeed.backend.domain.comment.entity.Comment;
import com.shelfeed.backend.domain.comment.entity.CommentLike;
import com.shelfeed.backend.domain.comment.repository.CommentLikeRepository;
import com.shelfeed.backend.domain.comment.repository.CommentRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.shelfeed.backend.domain.comment.entity.QComment.comment;

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

    //2. 댓글 조회
    public CommentListResponse getComments(Long reviewId, Long cursor, int limit, Long memberUserId){
        Review review = getReview(reviewId);

        List<Comment> parentComments = commentRepository.findParentComments(review,cursor, PageRequest.of(0, limit + 1));

        List<CommentResponse> content = parentComments.stream().map(comment ->{
                    //로그인 한 사람인가
                    boolean isMine = memberUserId != null && comment.getMember().getMemberUserId().equals(memberUserId);
                    //좋아요 누른 사람인가
                    boolean isLiked = memberUserId != null && commentLikeRepository.existsByComment_CommentIdAndMember_MemberUserId(comment.getCommentId(),memberUserId);

                    List<ReplyResponse> replies = commentRepository.findByParentComment(comment).stream().map(reply -> {
                        //로그인 한 유저 그리고 답글의 유저가 기존 멤버인가
                        boolean replyIsMine = memberUserId != null && reply.getMember().getMemberUserId().equals(memberUserId);
                        // 로그인 한 유저 그리고 좋아요를 눌렀는가
                        boolean replyIsLiked = memberUserId != null && commentLikeRepository.existsByComment_CommentIdAndMember_MemberUserId(reply.getCommentId(),memberUserId);
                        return ReplyResponse.of(reply,replyIsMine,replyIsLiked);
                }).toList();
                    return CommentResponse.of(comment,isMine,isLiked,replies);
        }).toList();
        return CommentListResponse.of(content,limit);
    }

    // 3. 댓글 수정
    @Transactional
    public CommentUpdateResponse updateComment(Long reviewId, Long commentId, Long memberUserId, CommentUpdateRequest request){
        Comment comment = getComment(commentId);
        //리뷰 없으면
        if (!comment.getReview().getReviewId().equals(reviewId)){
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        //작성한 유저가 없으면
        if (!comment.getMember().getMemberUserId().equals(memberUserId)){
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
        }
        comment.update(request.getContent());

        return CommentUpdateResponse.of(comment);
    }
    // 4. 댓글 삭제
    @Transactional
    public void deleteComment(Long reviewId, Long commentId, Long memberUserId){
        Comment comment = getComment(commentId);
        //리뷰 없으면
        if (!comment.getReview().getReviewId().equals(reviewId)){
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        //작성한 유저가 없으면
        if (!comment.getMember().getMemberUserId().equals(memberUserId)){
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
        }
        comment.softDelete();;
        comment.getReview().decreaseCommentCount();
    }
    // 5. 댓글 좋아요
    @Transactional
    public CommentLikeResponse likeComment(Long reviewId, Long commentId, Long memberUserId) {
        Member member = getMember(memberUserId);
        Comment comment = getComment(commentId);
        if (!comment.getReview().getReviewId().equals(reviewId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (comment.getMember().getMemberUserId().equals(memberUserId)) {
            throw new BusinessException(ErrorCode.SELF_LIKE_NOT_ALLOWED);
        }
        if (commentLikeRepository.existsByComment_CommentIdAndMember_MemberUserId(commentId, memberUserId)){
            throw new BusinessException(ErrorCode.ALREADY_COMMENT_LIKED);
        }
        commentLikeRepository.save(CommentLike.create(member, comment));
        comment.increaseLikeCount();
        return CommentLikeResponse.of(comment);
    }

    // 6. 댓글 좋아요 취소
    @Transactional
    public CommentLikeResponse unlikeComment(Long reviewId, Long commentId, Long memberUserId) {
        Comment comment = getComment(commentId);
        if (!comment.getReview().getReviewId().equals(reviewId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        CommentLike commentLike = commentLikeRepository.findByComment_CommentIdAndMember_MemberUserId(commentId,memberUserId)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMENT_LIKE_NOT_FOUND));
        commentLikeRepository.delete(commentLike);
        comment.decreaseLikeCount();
        return CommentLikeResponse.of(comment);
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
