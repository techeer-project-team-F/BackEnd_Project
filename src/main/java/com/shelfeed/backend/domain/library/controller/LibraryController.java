package com.shelfeed.backend.domain.library.controller;

import com.shelfeed.backend.domain.library.dto.request.LibraryBookAddRequest;
import com.shelfeed.backend.domain.library.dto.respond.LibraryBookAddResponse;
import com.shelfeed.backend.domain.library.service.LibraryService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    //1. 서재 도서 추가 POST /api/v1/library
    @PostMapping("/library")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LibraryBookAddResponse> addBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LibraryBookAddRequest request){
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(201,  "도서가 서재에 추가되었습니다.", libraryService.addBook(memberUserId,request));
    }
    //2. 내 서제 목록 조회 GET /api/v1/library/me

    //3. 서제 도서 상세조회 GET /api/v1/library/{libraryBookId}

    //4. 독서 상태 변경 PATCH /api/v1/library/{libraryBookId}/status

    //5. 서제에서 도서 제거 DELETE /api/v1/library/{libraryBookId}

    //6. 타 유저 서제 목록 조회 GET /api/v1/members/{userId}/library


}
