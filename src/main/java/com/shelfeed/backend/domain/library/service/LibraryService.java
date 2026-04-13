package com.shelfeed.backend.domain.library.service;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.library.dto.request.LibraryBookAddRequest;
import com.shelfeed.backend.domain.library.dto.respond.LibraryBookAddResponse;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final LibraryRepository libraryRepository;

    //1. 서재 도서 추가
    @Transactional
    public LibraryBookAddResponse addBook(Long memberUserId, LibraryBookAddRequest request){
        Member member = getMember(memberUserId);
        Book book = getbook(request.getBookId());
        //이미 서제에 있으면 에러
        if (libraryRepository.existsByMemberIdAndBook_BookId(member, request.getBookId())){
            throw new BusinessException(ErrorCode.ALREADY_IN_LIBRARY);
        }
        LibraryBook libraryBook = LibraryBook.create(member, book, request.getStatus());
        libraryRepository.save(libraryBook);
        return LibraryBookAddResponse.of(libraryBook);

    }

    //2. 내 서제 목록 조회

    //3. 서제 도서 상세조회

    //4. 독서 상태 변경

    //5. 서제에서 도서 제거

    //6. 타 유저 서제 목록 조회

    //헬퍼 메소드
    //멤버 찾기
    private Member getMember(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
    //책 찾기
    private Book getbook(Long bookId){
        return bookRepository.findById(bookId).orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
    }



}
