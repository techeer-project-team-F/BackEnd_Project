package com.shelfeed.backend.domain.book.controller;

import com.shelfeed.backend.domain.book.dto.request.BookSearchRequest;
import com.shelfeed.backend.domain.book.dto.respond.BookDetailResponse;
import com.shelfeed.backend.domain.book.dto.respond.BookSearchListResponse;
import com.shelfeed.backend.domain.book.service.BookService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // 도서 검색  GET /api/v1/books/search
    @GetMapping("/search")
    public ApiResponse<BookSearchListResponse> searchBooks(
            @ModelAttribute BookSearchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(200, bookService.searchBooks(request, memberUserId));
    }

    // 도서 상세 조회  GET /api/v1/books/{bookId}
    @GetMapping("/{bookId}")
    public ApiResponse<BookDetailResponse> getBookDetail(
            @PathVariable Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(200, bookService.getBookDetail(bookId, memberUserId));
    }
}
