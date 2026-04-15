package com.shelfeed.backend.domain.search.controller;

import com.shelfeed.backend.domain.search.dto.response.SearchResponse;
import com.shelfeed.backend.domain.search.service.SearchService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 10.1 통합 검색  GET /api/v1/search
    @GetMapping
    public ApiResponse<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberUserId = userDetails != null ? userDetails.getMember().getMemberUserId() : null;
        return ApiResponse.success(200,
                searchService.search(query, type, cursor, limit, memberUserId));
    }
}