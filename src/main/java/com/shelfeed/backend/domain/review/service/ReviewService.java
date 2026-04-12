package com.shelfeed.backend.domain.review.service;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.dto.request.ReviewCreateRequest;
import com.shelfeed.backend.domain.review.dto.response.ReviewCreateResponse;
import com.shelfeed.backend.domain.review.dto.response.ReviewDetailResponse;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.entity.ReviewTag;
import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.domain.review.repository.ReviewTagRepository;
import com.shelfeed.backend.domain.review.repository.TagRepository;
import com.shelfeed.backend.domain.tag.entity.Tag;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final TagRepository tagRepository;
    private final ReviewTagRepository reviewTagRepository;

    // ── 1 감상 작성
    @Transactional
    public ReviewCreateResponse createReview(Long memberUserId, ReviewCreateRequest request) {
        Member member = getMember(memberUserId);
        Book book = getBook(request.getBookId());
        //글이랑 인용구 둘 중 하난 필요
        if (request.getContent() == null && request.getQuote() == null) {
            throw new BusinessException(ErrorCode.CONTENT_OR_QUOTE_REQUIRED);
        }
        //동일한 도서를 중복으로 감상 눌렀을 떄 에러
        if (reviewRepository.existsByMemberAndBook_BookIdAndIsDeletedFalse(member, request.getBookId())){
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }
        //빈값을 보낼 때 DB에 책이 없을 때 null 값
        LibraryBook libraryBook = null;
        if (request.getLibraryBookId() != null) {
            libraryBook = libraryRepository.findById(request.getLibraryBookId()).orElse(null);
        }
        //감상 저장
        Review review = Review.create(member, book, libraryBook, request.getRating(), request.getContent(), request.getQuote(),
                request.isSpoiler(), request.getReadPages(), request.getReviewVisibility(), request.getReviewStatus());
        reviewRepository.save(review);

        //테그 처리(List로 나옴)
        List<String> tagNames = saveTags(review, request.getTags());
        //요청상태가 계시된 상태라면 리뷰 카운트 1 up
        if (request.getReviewStatus() == ReviewStatus.PUBLISHED) {
            member.increaseReviewCount();
        }
        return ReviewCreateResponse.of(review, tagNames);
    }

    //2. 감상 상세 조회 작성 API
    public ReviewDetailResponse getReview(Long reviewId, Long memberUserId){
        Review review = getReviewOrThrow(reviewId);//삭제 안된 리뷰 여부(소프트 델리트)
        boolean isMine = memberUserId != null && review.getMember().getMemberUserId().equals(memberUserId);//본인확인
        if (!isMine && review.getReviewVisibility().name().equals("PRIVATE")){//비공개 거나 게시물 주인이 아니면
            throw new BusinessException(ErrorCode.PRIVATE_REVIEW);//비공개 감상
        }
        List<String> tags = getTagNames(review);
        return ReviewDetailResponse.of(review,tags,isMine);
    }


    //헬퍼 메소드
    private Member getMember(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Book getBook(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(()-> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
    }
    //태그 저장
    private List<String> saveTags(Review review, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return List.of();
        //해당 이름의 태그가 있으면 그대로 쓰고 없으면 새로 만들어서 저장하고 그 태그를 현재 리뷰와 연결 후 저장
        return tagNames.stream().map(name ->{
            Tag tag = tagRepository.findByTagName(name).orElseGet(()-> tagRepository.save(Tag.create(name)));//orElseGet : Optioal 값이 null일때만 람다 실행
            reviewTagRepository.save(ReviewTag.create(review, tag)); // 리뷰,테그 다대다 풀어주는 데이터 생성
            return name;
        }).toList();
    }
    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId).orElseThrow(()-> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }
    //태그 이름 목록 조회
    private List<String> getTagNames(Review review) {
        return reviewTagRepository.findByReview(review).stream()
                .map(reviewTag -> reviewTag.getTag().getTagName()).toList();
    }

}

