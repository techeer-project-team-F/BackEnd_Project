package com.shelfeed.backend.domain.book.service;

import com.shelfeed.backend.domain.book.client.AladinApiClient;
import com.shelfeed.backend.domain.book.dto.external.AladinBookItem;
import com.shelfeed.backend.domain.book.dto.external.AladinSearchResponse;
import com.shelfeed.backend.domain.book.dto.request.BookSearchRequest;
import com.shelfeed.backend.domain.book.dto.respond.BookDetailResponse;
import com.shelfeed.backend.domain.book.dto.respond.BookSearchListResponse;
import com.shelfeed.backend.domain.book.dto.respond.BookSummaryResponse;
import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import com.shelfeed.backend.domain.library.repository.LibraryRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.domain.review.repository.ReviewRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final AladinApiClient aladinApiClient;

    public BookSearchListResponse searchBooks(BookSearchRequest request, Long memberUserId) {
        int limit = Math.min(request.getLimit(), 50);
        int page = decodeCursorToPage(request.getCursor());

        Member member = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        AladinSearchResponse aladinResponse = aladinApiClient.searchBooks(request.getQuery(), page, limit);
        List<AladinBookItem> items = aladinResponse.getItem() != null ? aladinResponse.getItem() : List.of();

        // 유효한 isbn13 목록 추출
        List<String> isbn13List = items.stream()
                .map(AladinBookItem::getIsbn13)
                .filter(isbn -> isbn != null && !isbn.isBlank())
                .collect(Collectors.toList());

        // DB에 이미 있는 책 배치 조회 (isbn13 → bookId 매핑)
        Map<String, Long> isbn13ToBookId = bookRepository.findByIsbn13In(isbn13List).stream()
                .collect(Collectors.toMap(Book::getIsbn13, Book::getBookId));

        // 사용자 서재에 있는 isbn13 배치 조회
        Set<String> libraryIsbn13Set = libraryRepository.findIsbn13InLibrary(member, isbn13List);

        List<BookSummaryResponse> responses = items.stream()
                .filter(item -> item.getIsbn13() != null && !item.getIsbn13().isBlank())
                .map(item -> {
                    Long bookId = isbn13ToBookId.get(item.getIsbn13());
                    boolean inMyLibrary = libraryIsbn13Set.contains(item.getIsbn13());

                    String author = item.getAuthor() != null
                            ? item.getAuthor().replaceAll("\\s*\\(.*?\\)", "").trim()
                            : null;
                    LocalDate publishedDate = null;
                    if (item.getPubDate() != null && !item.getPubDate().isBlank()) {
                        try {
                            publishedDate = LocalDate.parse(item.getPubDate());
                        } catch (Exception ignored) {}
                    }

                    return BookSummaryResponse.builder()
                            .bookId(bookId)
                            .isbn13(item.getIsbn13())
                            .title(item.getTitle())
                            .author(author)
                            .publisher(item.getPublisher())
                            .coverImageUrl(item.getCover())
                            .publishedDate(publishedDate)
                            .inMyLibrary(inMyLibrary)
                            .build();
                })
                .collect(Collectors.toList());

        boolean hasNext = aladinResponse.getTotalResults() > (long) page * limit;
        String nextCursor = hasNext ? encodeCursorFromPage(page + 1) : null;

        return BookSearchListResponse.builder()
                .content(responses)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(responses.size())
                .build();
    }

    // 도서 상세 조회  GET /api/v1/books/{bookId}
    public BookDetailResponse getBookDetail(Long bookId, Long memberUserId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        Member member = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Double averageRating = bookRepository.findAverageRatingByBookId(bookId);
        Long reviewCount = bookRepository.countReviewsByBookId(bookId);

        ReadingStatus myLibraryStatus = libraryRepository.findByMemberIdAndBook_BookId(member, bookId)
                .map(lb -> lb.getStatus())
                .orElse(null);

        Long myReviewId = reviewRepository.findByMemberAndBook_BookIdAndIsDeletedFalse(member, bookId)
                .map(Review::getReviewId)
                .orElse(null);

        return BookDetailResponse.of(book, averageRating, reviewCount, myLibraryStatus, myReviewId);
    }

    private int decodeCursorToPage(String cursor) {
        if (cursor == null || cursor.isBlank()) return 1;
        try {
            String json = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String pageStr = json.replaceAll(".*\"page\"\\s*:\\s*(\\d+).*", "$1");
            return Integer.parseInt(pageStr);
        } catch (Exception e) {
            return 1;
        }
    }

    private String encodeCursorFromPage(int page) {
        String json = "{\"page\":" + page + "}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
