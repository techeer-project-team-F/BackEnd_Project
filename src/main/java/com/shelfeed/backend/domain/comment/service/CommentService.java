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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<Comment> parentComments = commentRepository.findParentComments(review, cursor, PageRequest.of(0, limit + 1));
        boolean hasNext = parentComments.size() > limit;
        if (hasNext) parentComments = parentComments.subList(0, limit);

        // 대댓글 IN절 일괄 조회
        List<Comment> allReplies = parentComments.isEmpty()
                ? List.of()
                : commentRepository.findRepliesByParents(parentComments);
        Map<Long, List<Comment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(r -> r.getParentComment().getCommentId()));

        // 좋아요 IN절 일괄 조회 (부모 댓글 + 대댓글 한번에)
        Set<Long> likedIds = Set.of();
        if (memberUserId != null) {
            List<Long> allCommentIds = new ArrayList<>();
            parentComments.forEach(c -> allCommentIds.add(c.getCommentId()));
            allReplies.forEach(r -> allCommentIds.add(r.getCommentId()));
            if (!allCommentIds.isEmpty()) {
                likedIds = commentLikeRepository.findLikedCommentIds(allCommentIds, memberUserId);
            }
        }

        final Set<Long> finalLikedIds = likedIds;
        List<CommentResponse> content = parentComments.stream().map(comment -> {
            boolean isMine = memberUserId != null && comment.getMember().getMemberUserId().equals(memberUserId);
            boolean isLiked = finalLikedIds.contains(comment.getCommentId());

            List<ReplyResponse> replies = repliesMap.getOrDefault(comment.getCommentId(), List.of()).stream()
                    .map(reply -> {
                        boolean replyIsMine = memberUserId != null && reply.getMember().getMemberUserId().equals(memberUserId);
                        boolean replyIsLiked = finalLikedIds.contains(reply.getCommentId());
                        return ReplyResponse.of(reply, replyIsMine, replyIsLiked);
                    }).toList();
            return CommentResponse.of(comment, isMine, isLiked, replies);
        }).toList();
        return CommentListResponse.of(content, limit);
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
