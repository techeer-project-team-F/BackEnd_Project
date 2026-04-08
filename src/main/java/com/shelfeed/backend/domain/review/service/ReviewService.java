package com.shelfeed.backend.domain.review.service;
// 도서 쪽 레포지토리를 만들어야 가능할 거 같다

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.dto.request.ReviewCreateRequest;
import com.shelfeed.backend.domain.review.dto.response.ReviewCreateResponse;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    // ── 1 감상 작성
    public ReviewCreateResponse createReview(Long memberUserId, ReviewCreateRequest request) {
        Member member = getMember(memberUserId);

        if (request.getContent() == null && request.getQuote() == null) {//글이랑 인용구 둘 중 하난 필
            throw new BusinessException(ErrorCode.CONTENT_OR_QUOTE_REQUIRED);
        }

        if (reviewRepository.existsByMemberAndBook_BookIdAndIsDeletedFalse(member, request.getBookId())){
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);//중복 감상이면 에러
        }





return ReviewCreateResponse.of()
    }

    private Member getMember(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
*/
