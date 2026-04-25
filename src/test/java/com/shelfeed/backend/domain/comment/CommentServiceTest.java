package com.shelfeed.backend.domain.comment;

import com.shelfeed.backend.domain.block.repository.BlockRepository;
import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.comment.dto.request.CommentCreateRequest;
import com.shelfeed.backend.domain.comment.dto.request.CommentUpdateRequest;
import com.shelfeed.backend.domain.comment.entity.Comment;
import com.shelfeed.backend.domain.comment.entity.CommentLike;
import com.shelfeed.backend.domain.comment.repository.CommentLikeRepository;
import com.shelfeed.backend.domain.comment.repository.CommentRepository;
import com.shelfeed.backend.domain.comment.service.CommentService;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock CommentRepository commentRepository;
    @Mock CommentLikeRepository commentLikeRepository;
    @Mock BlockRepository blockRepository;

    @InjectMocks CommentService commentService;

    private Member ownerMember;      // 감상 작성자 (id=1)
    private Member commenterMember;  // 댓글 작성자 (id=2)
    private Review publicReview;     // PUBLIC 감상 (reviewId=10)
    private Review privateReview;    // PRIVATE 감상 (reviewId=11)
    private Comment parentComment;   // 원댓글 (commentId=100, member=commenterMember)
    private Comment replyComment;    // 대댓글 (commentId=101, parentComment=parentComment)

    @BeforeEach
    void setUp() {
        ownerMember    = Member.createLocal(1L, "owner@test.com",     "pw", "작성자",  "bio");
        commenterMember = Member.createLocal(2L, "commenter@test.com", "pw", "댓글러", "bio");

        Book book = Book.create("9791234567890", "테스트 도서", "저자", "출판사",
                null, null, null, null, null, null);

        publicReview = Review.create(ownerMember, book, null, (byte) 4, "공개 감상", null,
                false, null, ReviewVisibility.PUBLIC, ReviewStatus.PUBLISHED);
        ReflectionTestUtils.setField(publicReview, "reviewId", 10L);

        privateReview = Review.create(ownerMember, book, null, (byte) 4, "비공개 감상", null,
                false, null, ReviewVisibility.PRIVATE, ReviewStatus.PUBLISHED);
        ReflectionTestUtils.setField(privateReview, "reviewId", 11L);

        parentComment = Comment.createOriginComment(publicReview, commenterMember, "원댓글 내용");
        ReflectionTestUtils.setField(parentComment, "commentId", 100L);

        replyComment = Comment.createReply(publicReview, commenterMember, parentComment, "대댓글 내용");
        ReflectionTestUtils.setField(replyComment, "commentId", 101L);
    }

    // ────────────────────────────────────────────────────────
    // 1. createComment()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("댓글 작성")
    class CreateComment {

        @Test
        @DisplayName("존재하지 않는 회원이면 MEMBER_NOT_FOUND 예외가 발생한다")
        void 회원없음_예외() {
            given(memberRepository.findByMemberUserId(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createComment(10L, 99L, createRequest("내용", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("삭제된 감상에는 댓글을 작성할 수 없다")
        void 감상없음_예외() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createComment(99L, 2L, createRequest("내용", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        @DisplayName("비공개 감상에 타인이 댓글을 작성하면 PRIVATE_REVIEW 예외가 발생한다")
        void 비공개_감상_댓글_예외() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(11L)).willReturn(Optional.of(privateReview));

            assertThatThrownBy(() -> commentService.createComment(11L, 2L, createRequest("내용", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRIVATE_REVIEW);
        }

        @Test
        @DisplayName("차단 관계가 있으면 BLOCKED_USER 예외가 발생한다")
        void 차단_관계_댓글_예외() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(10L)).willReturn(Optional.of(publicReview));
            given(blockRepository.existsByBlockerAndBlocked(ownerMember, commenterMember)).willReturn(true);

            assertThatThrownBy(() -> commentService.createComment(10L, 2L, createRequest("내용", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BLOCKED_USER);
        }

        @Test
        @DisplayName("부모 댓글이 존재하지 않으면 PARENT_COMMENT_NOT_FOUND 예외가 발생한다")
        void 부모댓글없음_예외() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(10L)).willReturn(Optional.of(publicReview));
            given(blockRepository.existsByBlockerAndBlocked(ownerMember, commenterMember)).willReturn(false);
            given(blockRepository.existsByBlockerAndBlocked(commenterMember, ownerMember)).willReturn(false);
            given(commentRepository.findByCommentIdAndIsDeletedFalse(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createComment(10L, 2L, createRequest("내용", 99L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PARENT_COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("대댓글에 답글을 달면 NESTED_REPLY_NOT_ALLOWED 예외가 발생한다")
        void 대댓글에_답글_예외() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(10L)).willReturn(Optional.of(publicReview));
            given(blockRepository.existsByBlockerAndBlocked(ownerMember, commenterMember)).willReturn(false);
            given(blockRepository.existsByBlockerAndBlocked(commenterMember, ownerMember)).willReturn(false);
            // replyComment의 parentComment != null → 대댓글에 대한 답글
            given(commentRepository.findByCommentIdAndIsDeletedFalse(101L)).willReturn(Optional.of(replyComment));

            assertThatThrownBy(() -> commentService.createComment(10L, 2L, createRequest("내용", 101L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NESTED_REPLY_NOT_ALLOWED);
        }

        @Test
        @DisplayName("원댓글 작성 성공 시 댓글을 저장하고 commentCount를 증가시킨다")
        void 원댓글_작성_성공() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(10L)).willReturn(Optional.of(publicReview));
            given(blockRepository.existsByBlockerAndBlocked(ownerMember, commenterMember)).willReturn(false);
            given(blockRepository.existsByBlockerAndBlocked(commenterMember, ownerMember)).willReturn(false);

            commentService.createComment(10L, 2L, createRequest("원댓글 내용", null));

            verify(commentRepository).save(any(Comment.class));
            verify(reviewRepository).increaseCommentCount(10L);
        }

        @Test
        @DisplayName("대댓글 작성 성공 시 댓글을 저장하고 commentCount를 증가시킨다")
        void 대댓글_작성_성공() {
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(10L)).willReturn(Optional.of(publicReview));
            given(blockRepository.existsByBlockerAndBlocked(ownerMember, commenterMember)).willReturn(false);
            given(blockRepository.existsByBlockerAndBlocked(commenterMember, ownerMember)).willReturn(false);
            // parentComment.getParentComment() == null → 답글 허용
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            commentService.createComment(10L, 2L, createRequest("대댓글 내용", 100L));

            verify(commentRepository).save(any(Comment.class));
            verify(reviewRepository).increaseCommentCount(10L);
        }
    }

    // ────────────────────────────────────────────────────────
    // 2. getComments()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("삭제된 감상이면 REVIEW_NOT_FOUND 예외가 발생한다")
        void 감상없음_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.getComments(99L, null, 10, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        @DisplayName("정상 조회 시 CommentListResponse를 반환한다")
        void 정상_댓글_목록_조회_성공() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(10L)).willReturn(Optional.of(publicReview));
            given(commentRepository.findParentComments(eq(publicReview), any(), any())).willReturn(List.of());

            var result = commentService.getComments(10L, null, 10, null);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.isHasNext()).isFalse();
        }
    }

    // ────────────────────────────────────────────────────────
    // 3. updateComment()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("삭제된 댓글이면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 삭제된_댓글_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.updateComment(10L, 100L, 2L, updateRequest("수정 내용")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("댓글이 해당 감상에 속하지 않으면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 다른_감상_댓글_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            // reviewId=99L ≠ parentComment.review.reviewId=10L
            assertThatThrownBy(() -> commentService.updateComment(99L, 100L, 2L, updateRequest("수정 내용")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("본인 댓글이 아니면 NOT_COMMENT_OWNER 예외가 발생한다")
        void 타인_댓글_수정_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            // memberUserId=1L ≠ parentComment.member.memberUserId=2L
            assertThatThrownBy(() -> commentService.updateComment(10L, 100L, 1L, updateRequest("수정 내용")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_COMMENT_OWNER);
        }

        @Test
        @DisplayName("정상 수정 시 댓글 내용이 변경된다")
        void 정상_댓글_수정_성공() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            commentService.updateComment(10L, 100L, 2L, updateRequest("수정된 내용"));

            assertThat(parentComment.getContent()).isEqualTo("수정된 내용");
        }
    }

    // ────────────────────────────────────────────────────────
    // 4. deleteComment()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("삭제된 댓글이면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 삭제된_댓글_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.deleteComment(10L, 100L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("댓글이 해당 감상에 속하지 않으면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 다른_감상_댓글_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            assertThatThrownBy(() -> commentService.deleteComment(99L, 100L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("본인 댓글이 아니면 NOT_COMMENT_OWNER 예외가 발생한다")
        void 타인_댓글_삭제_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            assertThatThrownBy(() -> commentService.deleteComment(10L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_COMMENT_OWNER);
        }

        @Test
        @DisplayName("정상 삭제 시 소프트 삭제하고 commentCount를 감소시킨다")
        void 정상_댓글_삭제_성공() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            commentService.deleteComment(10L, 100L, 2L);

            assertThat(parentComment.isDeleted()).isTrue();
            verify(reviewRepository).decreaseCommentCount(10L);
        }
    }

    // ────────────────────────────────────────────────────────
    // 5. likeComment()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("댓글 좋아요")
    class LikeComment {

        @Test
        @DisplayName("존재하지 않는 회원이면 MEMBER_NOT_FOUND 예외가 발생한다")
        void 회원없음_예외() {
            given(memberRepository.findByMemberUserId(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.likeComment(10L, 100L, 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("삭제된 댓글이면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 삭제된_댓글_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(ownerMember));
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.likeComment(10L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("댓글이 해당 감상에 속하지 않으면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 다른_감상_댓글_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(ownerMember));
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            assertThatThrownBy(() -> commentService.likeComment(99L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("본인 댓글에 좋아요를 누르면 SELF_LIKE_NOT_ALLOWED 예외가 발생한다")
        void 셀프_좋아요_예외() {
            // commenterMember(2L)가 자신의 댓글(parentComment.member=2L)에 좋아요
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(commenterMember));
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            assertThatThrownBy(() -> commentService.likeComment(10L, 100L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SELF_LIKE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("이미 좋아요한 댓글이면 ALREADY_COMMENT_LIKED 예외가 발생한다")
        void 중복_좋아요_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(ownerMember));
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));
            given(commentLikeRepository.existsByComment_CommentIdAndMember_MemberUserId(100L, 1L))
                    .willReturn(true);

            assertThatThrownBy(() -> commentService.likeComment(10L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ALREADY_COMMENT_LIKED);
        }

        @Test
        @DisplayName("정상 좋아요 시 CommentLike를 저장하고 likeCount를 증가시킨다")
        void 정상_좋아요_성공() {
            // ownerMember(1L)가 commenterMember(2L)의 댓글에 좋아요
            // likeComment는 내부에서 getComment를 2번 호출 (로직 + 응답용)
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(ownerMember));
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L))
                    .willReturn(Optional.of(parentComment));
            given(commentLikeRepository.existsByComment_CommentIdAndMember_MemberUserId(100L, 1L))
                    .willReturn(false);

            var result = commentService.likeComment(10L, 100L, 1L);

            assertThat(result).isNotNull();
            verify(commentLikeRepository).save(any(CommentLike.class));
            verify(commentRepository).increaseLikeCount(100L);
        }
    }

    // ────────────────────────────────────────────────────────
    // 6. unlikeComment()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("댓글 좋아요 취소")
    class UnlikeComment {

        @Test
        @DisplayName("삭제된 댓글이면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 삭제된_댓글_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.unlikeComment(10L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("댓글이 해당 감상에 속하지 않으면 COMMENT_NOT_FOUND 예외가 발생한다")
        void 다른_감상_댓글_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));

            assertThatThrownBy(() -> commentService.unlikeComment(99L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("좋아요 내역이 없으면 COMMENT_LIKE_NOT_FOUND 예외가 발생한다")
        void 좋아요_내역없음_예외() {
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L)).willReturn(Optional.of(parentComment));
            given(commentLikeRepository.findByComment_CommentIdAndMember_MemberUserId(100L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.unlikeComment(10L, 100L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMMENT_LIKE_NOT_FOUND);
        }

        @Test
        @DisplayName("정상 취소 시 CommentLike를 삭제하고 likeCount를 감소시킨다")
        void 정상_좋아요_취소_성공() {
            // unlikeComment도 내부에서 getComment를 2번 호출 (로직 + 응답용)
            CommentLike like = mock(CommentLike.class);
            given(commentRepository.findByCommentIdAndIsDeletedFalse(100L))
                    .willReturn(Optional.of(parentComment));
            given(commentLikeRepository.findByComment_CommentIdAndMember_MemberUserId(100L, 1L))
                    .willReturn(Optional.of(like));

            commentService.unlikeComment(10L, 100L, 1L);

            verify(commentLikeRepository).delete(like);
            verify(commentRepository).decreaseLikeCount(100L);
        }
    }

    // ────────────────────────────────────────────────────────
    // 픽스처 헬퍼 메서드
    // ────────────────────────────────────────────────────────

    private CommentCreateRequest createRequest(String content, Long parentCommentId) {
        CommentCreateRequest req = new CommentCreateRequest();
        ReflectionTestUtils.setField(req, "content", content);
        ReflectionTestUtils.setField(req, "parentCommentId", parentCommentId);
        return req;
    }

    private CommentUpdateRequest updateRequest(String content) {
        CommentUpdateRequest req = new CommentUpdateRequest();
        ReflectionTestUtils.setField(req, "content", content);
        return req;
    }
}
