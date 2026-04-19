package com.shelfeed.backend.domain.review;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.dto.request.ReviewCreateRequest;
import com.shelfeed.backend.domain.review.dto.request.ReviewUpdateRequest;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import com.shelfeed.backend.domain.review.repository.ReviewLikeRepository;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.domain.review.repository.ReviewTagRepository;
import com.shelfeed.backend.domain.review.repository.TagRepository;
import com.shelfeed.backend.domain.review.service.ReviewService;
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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock LibraryRepository libraryRepository;
    @Mock BookRepository bookRepository;
    @Mock TagRepository tagRepository;
    @Mock ReviewTagRepository reviewTagRepository;
    @Mock ReviewLikeRepository reviewLikeRepository;
    @Mock FeedRepository feedRepository;

    @InjectMocks ReviewService reviewService;

    private Member owner;
    private Member other;
    private Book book;
    private Review publicReview;
    private Review privateReview;

    @BeforeEach
    void setUp() {
        owner = Member.createLocal(1L, "owner@test.com", "encoded", "작성자", "bio");
        other = Member.createLocal(2L, "other@test.com", "encoded", "타인", "bio");

        book = Book.create("9791234567890", "테스트 책", "작가", "출판사",
                null, null, null, null, null, null);

        publicReview = Review.create(owner, book, null, (byte) 5, "내용", null,
                false, null, ReviewVisibility.PUBLIC, ReviewStatus.PUBLISHED);

        privateReview = Review.create(owner, book, null, (byte) 5, "내용", null,
                false, null, ReviewVisibility.PRIVATE, ReviewStatus.PUBLISHED);
    }

    // ── 헬퍼: ReviewCreateRequest 생성 ──────────────────────
    private ReviewCreateRequest createRequest(Long bookId, String content, String quote) {
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReflectionTestUtils.setField(request, "bookId", bookId);
        ReflectionTestUtils.setField(request, "content", content);
        ReflectionTestUtils.setField(request, "quote", quote);
        return request;
    }

    // ── 헬퍼: ReviewUpdateRequest 생성 ──────────────────────
    private ReviewUpdateRequest updateRequest(String content, String quote) {
        ReviewUpdateRequest request = new ReviewUpdateRequest();
        ReflectionTestUtils.setField(request, "content", content);
        ReflectionTestUtils.setField(request, "quote", quote);
        return request;
    }

    // ────────────────────────────────────────────────────────
    // createReview()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("감상 작성")
    class CreateReview {

        @Test
        @DisplayName("content와 quote가 모두 null이면 CONTENT_OR_QUOTE_REQUIRED 예외가 발생한다")
        void content_quote_둘다_null_예외() {
            ReviewCreateRequest request = createRequest(1L, null, null);
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(owner));
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));

            assertThatThrownBy(() -> reviewService.createReview(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CONTENT_OR_QUOTE_REQUIRED);
        }

        @Test
        @DisplayName("동일 도서에 감상을 중복 작성하면 DUPLICATE_REVIEW 예외가 발생한다")
        void 중복_감상_작성_예외() {
            ReviewCreateRequest request = createRequest(1L, "내용", null);
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(owner));
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(reviewRepository.existsByMemberAndBook_BookIdAndIsDeletedFalse(owner, 1L)).willReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_REVIEW);
        }
    }

    // ────────────────────────────────────────────────────────
    // getReview()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("감상 조회")
    class GetReview {

        @Test
        @DisplayName("비공개 감상을 타인이 조회하면 PRIVATE_REVIEW 예외가 발생한다")
        void 비공개_감상_타인_조회_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(privateReview));

            assertThatThrownBy(() -> reviewService.getReview(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRIVATE_REVIEW);
        }

        @Test
        @DisplayName("비공개 감상을 작성자 본인이 조회하면 성공한다")
        void 비공개_감상_본인_조회_성공() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(privateReview));
            given(reviewTagRepository.findByReview(privateReview)).willReturn(List.of());

            assertThatNoException().isThrownBy(() -> reviewService.getReview(1L, 1L));
        }

        @Test
        @DisplayName("비로그인 사용자가 비공개 감상을 조회하면 PRIVATE_REVIEW 예외가 발생한다")
        void 비공개_감상_비로그인_조회_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(privateReview));

            assertThatThrownBy(() -> reviewService.getReview(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRIVATE_REVIEW);
        }
    }

    // ────────────────────────────────────────────────────────
    // updateReview()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("감상 수정")
    class UpdateReview {

        @Test
        @DisplayName("본인 감상이 아닌 경우 NOT_REVIEW_OWNER 예외가 발생한다")
        void 타인_감상_수정_예외() {
            ReviewUpdateRequest request = updateRequest("내용", null);
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));

            assertThatThrownBy(() -> reviewService.updateReview(1L, 2L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_REVIEW_OWNER);
        }

        @Test
        @DisplayName("수정 시 content와 quote가 모두 null이면 CONTENT_OR_QUOTE_REQUIRED 예외가 발생한다")
        void 수정_content_quote_둘다_null_예외() {
            ReviewUpdateRequest request = updateRequest(null, null);
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));

            assertThatThrownBy(() -> reviewService.updateReview(1L, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CONTENT_OR_QUOTE_REQUIRED);
        }
    }

    // ────────────────────────────────────────────────────────
    // deleteReview()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("감상 삭제")
    class DeleteReview {

        @Test
        @DisplayName("본인 감상이 아닌 경우 NOT_REVIEW_OWNER 예외가 발생한다")
        void 타인_감상_삭제_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));

            assertThatThrownBy(() -> reviewService.deleteReview(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_REVIEW_OWNER);
        }

        @Test
        @DisplayName("PUBLISHED 감상 삭제 시 isDeleted 상태가 true로 변경되고 카운트가 감소한다")
        void PUBLISHED_감상_삭제_검증() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));

            reviewService.deleteReview(1L, 1L);

            assertThat(publicReview.isDeleted()).isTrue();
            verify(memberRepository).decreaseReviewCount(1L);
            verify(feedRepository).deleteByReview(publicReview);
        }
    }

    // ────────────────────────────────────────────────────────
    // likeReview()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("감상 좋아요")
    class LikeReview {

        @Test
        @DisplayName("본인 감상에 좋아요하면 SELF_LIKE_NOT_ALLOWED 예외가 발생한다")
        void 본인_감상_좋아요_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(owner));

            assertThatThrownBy(() -> reviewService.likeReview(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SELF_LIKE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("이미 좋아요한 감상에 다시 좋아요하면 ALREADY_REVIEW_LIKED 예외가 발생한다")
        void 중복_좋아요_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(other));
            given(reviewLikeRepository.existsByReview_ReviewIdAndMember_MemberUserId(1L, 2L))
                    .willReturn(true);

            assertThatThrownBy(() -> reviewService.likeReview(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ALREADY_REVIEW_LIKED);
        }
    }

    // ────────────────────────────────────────────────────────
    // unlikeReview()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("감상 좋아요 취소")
    class UnlikeReview {

        @Test
        @DisplayName("좋아요 내역이 없으면 REVIEW_LIKE_NOT_FOUND 예외가 발생한다")
        void 좋아요_없음_예외() {
            given(reviewRepository.findByReviewIdAndIsDeletedFalse(1L))
                    .willReturn(Optional.of(publicReview));
            given(reviewLikeRepository.findByReview_ReviewIdAndMember_MemberUserId(1L, 2L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.unlikeReview(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.REVIEW_LIKE_NOT_FOUND);
        }
    }
}
