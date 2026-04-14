package com.shelfeed.backend.domain.book.service;

import com.shelfeed.backend.domain.book.client.AladinApiClient;
import com.shelfeed.backend.domain.book.client.dto.AladinItem;
import com.shelfeed.backend.domain.book.client.dto.AladinSearchResponse;
import com.shelfeed.backend.domain.book.dto.request.BookReviewSearchRequest;
import com.shelfeed.backend.domain.book.dto.request.BookSearchRequest;
import com.shelfeed.backend.domain.book.dto.respond.*;
import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.repository.ReviewLikeRepository;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LibraryRepository libraryRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final AladinApiClient aladinApiClient;


    // 1. 도서 검색
    @Transactional
    public BookSearchListResponse searchBooks(BookSearchRequest request, Long memberUserId) {
        AladinSearchResponse response = aladinApiClient.search(request.getQuery(), 1 , request.getLimit() +1); //무한스크롤을 위해 +1 개 더 조회
    if (response == null || response.getItems() == null) {
        return BookSearchListResponse.of(List.of(), request.getLimit());// 내용없으면 빈 리스트
    }

    List<Book> books = response.getItems().stream().map(this::findOrCreateBook).toList();//getItems들을 findOrCreateBook 넣어라
    Member member= memberUserId != null ? getMemberOrNull(memberUserId) : null;//멤버 없으면 null 있으면 사용

    //책이 유저의 서재에 담겨 있는가
    List<BookSummaryResponse> content = books.stream().map(book -> { //각 책들을
        boolean inMyLibrary = member != null && libraryRepository.existsByMemberIdAndBook_BookId(member, book.getBookId());//로그인이 되어있고 회원이 저장한 책이라면
        return BookSummaryResponse.of(book,inMyLibrary);
    }).toList();

    return BookSearchListResponse.of(content, request.getLimit());
    }


    // 2. 도서 상세 조회
    public BookDetailResponse getBook(Long bookId, Long memberUserId) {
        //책 없으면 예외
        Book book = bookRepository.findById(bookId).orElseThrow(()->new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        Double averageRating = bookRepository.findAverageRatingByBookId(bookId);//평균 별점
        Long reviewCount = bookRepository.countReviewsByBookId(bookId);//리뷰 카운트
        ReadingStatus myLibraryStatus = null;
        Long myReviewId = null;

        if (memberUserId != null){Member member = getMemberOrNull(memberUserId);//멤버 있으면 넣고 없으면 null
            if (member != null){

                Optional<LibraryBook> libraryBook = libraryRepository.findByMemberIdAndBook_BookId(member,bookId);
                if (libraryBook.isPresent()){//isPresent : Optional에 데이터가 있으면 true 없으면 false
                    myLibraryStatus = libraryBook.get().getStatus();
                    }

                Optional<Review> myReview = reviewRepository.findByMemberAndBook_BookIdAndIsDeletedFalse(member,bookId);
                if (myReview.isPresent()){
                    myReviewId = myReview.get().getReviewId();
                }
            }
        }
        return BookDetailResponse.of(book,averageRating,reviewCount,myLibraryStatus,myReviewId);
    }

    // 3. ISBN 조회
    @Transactional
    public BookDetailResponse getBookByIsbn(String isbn13, Long memberUserId) {
        Optional<Book> existing = bookRepository.findByIsbn13(isbn13);
        Book book = existing.orElseGet(() -> {
            AladinSearchResponse response = aladinApiClient.lookupByIsbn(isbn13);
            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
            }
            return findOrCreateBook(response.getItems().get(0));
        });

        boolean inMyLibrary = false;
        if (memberUserId != null) {
            Member member = getMemberOrNull(memberUserId);
            if (member != null) {
                inMyLibrary = libraryRepository.existsByMemberIdAndBook_BookId(member, book.getBookId());
            }
        }
        return BookDetailResponse.ofIsbn(book, inMyLibrary);
    }

    // 4. 도서별 감상 목록
    public BookReviewListResponse getBookReviews(Long bookId, BookReviewSearchRequest request, Long memberUserId){
        if (!bookRepository.existsById(bookId)){
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        int pageSize = request.getLimit() + 1;
        List<Review> reviews = switch (request.getSort()){//필터링
            case "popular" -> reviewRepository.findBookReviewsPopular(bookId, request.getCursor(), PageRequest.of(0,pageSize));
            case "rating_high" -> reviewRepository.findBookReviewsRatingHigh(bookId, PageRequest.of(0, pageSize));
            case "rating_low"  -> reviewRepository.findBookReviewsRatingLow(bookId, PageRequest.of(0, pageSize));
            default -> reviewRepository.findBookReviewsLatest(
                    bookId, request.getCursor(), PageRequest.of(0, pageSize));
        };
        List<BookReviewResponse> content = reviews.stream().map(review -> {
                    boolean isLiked = memberUserId != null && reviewLikeRepository.existsByReview_ReviewIdAndMember_MemberUserId(review.getReviewId(), memberUserId);
                    return BookReviewResponse.of(review, isLiked);
                }).toList();

        return BookReviewListResponse.of(content, request.getLimit());
    }

    // 알라딘 아이템 → DB Book (없으면 저장)
    private Book findOrCreateBook(AladinItem item) {
        return bookRepository.findByIsbn13(item.getIsbn13())
                .orElseGet(() -> {
                    LocalDate pubDate = null;
                    try {
                        pubDate = LocalDate.parse(item.getPubDate());
                    } catch (Exception ignored) {} //날짜형식이 달라도 에러 안생기게
                    //페이지 있으면 넣고 아니면 말고
                    Integer totalPages = item.getSubInfo() != null ? item.getSubInfo().getItemPage() : null;

                    Book book = Book.create(
                            item.getIsbn13(),
                            item.getTitle(),
                            item.getAuthor(),
                            item.getPublisher(),
                            item.getCover(),
                            item.getDescription(),
                            totalPages,
                            pubDate,
                            item.getItemId() != null ? String.valueOf(item.getItemId()) : null,
                            item.getCategoryName()
                    );
                    return bookRepository.save(book);
                });
    }
    //맵버 찾거나 없으면 null
    private Member getMemberOrNull(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId).orElse(null);
    }
}
