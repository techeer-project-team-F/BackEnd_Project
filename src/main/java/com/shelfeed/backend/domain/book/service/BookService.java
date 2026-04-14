package com.shelfeed.backend.domain.book.service;

import com.shelfeed.backend.domain.book.client.AladinApiClient;
import com.shelfeed.backend.domain.book.client.dto.AladinItem;
import com.shelfeed.backend.domain.book.client.dto.AladinSearchResponse;
import com.shelfeed.backend.domain.book.dto.request.BookSearchRequest;
import com.shelfeed.backend.domain.book.dto.respond.BookSearchListResponse;
import com.shelfeed.backend.domain.book.dto.respond.BookSummaryResponse;
import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.repository.ReviewLikeRepository;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
    // 3. ISBN 조회
    // 4. 도서별 감상 목록


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
