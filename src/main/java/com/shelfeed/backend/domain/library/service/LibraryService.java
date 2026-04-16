package com.shelfeed.backend.domain.library.service;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.library.dto.request.LibraryBookAddRequest;
import com.shelfeed.backend.domain.library.dto.request.LibraryStatusUpdateRequest;
import com.shelfeed.backend.domain.library.dto.respond.*;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.LibraryVisibility;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public LibraryListResponse getMyLibrary(Long memberUserId, ReadingStatus status, Long cursor, int limit){
        Member member = getMember(memberUserId);
        //Id 기반 페이지 네이션
        List<LibraryBook> books = libraryRepository.findLibraryBooks(member, status, cursor, PageRequest.of(0, limit + 1));

        List<LibraryBookSummaryResponse> content = books.stream().map(LibraryBookSummaryResponse::of).toList();

        return  LibraryListResponse.of(content,limit);
    }

    //3. 서제 도서 상세조회
    public LibraryBookDetailResponse getLibraryBookDetail(Long libraryBookId, Long memberUserId){
        Member member = getMember(memberUserId);
        LibraryBook libraryBook = libraryRepository.findByLibraryBookIdAndMemberId(libraryBookId,member)
                .orElseThrow(()->new BusinessException(ErrorCode.LIBRARY_BOOK_NOT_FOUND));
        Review review = reviewRepository.findByMemberAndBook_BookIdAndIsDeletedFalse(member, libraryBook.getBook().getBookId())
                .orElse(null);
        return LibraryBookDetailResponse.of(libraryBook, review);
    }

    //4. 독서 상태 변경
    @Transactional
    public LibraryStatusUpdateResponse updateStatus(Long libraryBookId, Long memberUserId, LibraryStatusUpdateRequest request){
        Member member = getMember(memberUserId);
        LibraryBook libraryBook = libraryRepository.findByLibraryBookIdAndMemberId(libraryBookId,member)
                .orElseThrow(()->new BusinessException(ErrorCode.LIBRARY_BOOK_NOT_FOUND));

        libraryBook.updateStatus(request.getStatus());

        return LibraryStatusUpdateResponse.of(libraryBook);
    }

    //5. 서제에서 도서 제거
    @Transactional
    public void removeBook(Long libraryBookId, Long memberUserId){
        Member member = getMember(memberUserId);
        LibraryBook libraryBook = libraryRepository.findByLibraryBookIdAndMemberId(libraryBookId,member)
                .orElseThrow(()->new BusinessException(ErrorCode.LIBRARY_BOOK_NOT_FOUND));
        if (reviewRepository.existsByMember_MemberUserIdAndBook_BookIdAndIsDeletedFalse(memberUserId, libraryBook.getBook().getBookId())) {
            throw new BusinessException(ErrorCode.REVIEW_EXISTS);
        }
        libraryRepository.delete(libraryBook);
    }

    //6. 타 유저 서재 목록 조회
    public UserLibraryResponse getUserLibrary(Long userId, ReadingStatus status, Long cursor, int limit) {
        Member member = getMember(userId);

        // 비공개 서재면 빈 응답 반환
        if (member.getLibraryVisibility() == LibraryVisibility.PRIVATE) {
            return UserLibraryResponse.ofPrivate();
        }

        List<LibraryBook> books = libraryRepository.findLibraryBooks(member, status, cursor, PageRequest.of(0, limit + 1));
        List<LibraryBookSummaryResponse> content = books.stream().map(LibraryBookSummaryResponse::of).toList();
        return UserLibraryResponse.of(content, limit);
    }

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
