package com.shelfeed.backend.domain.report.controller;

import com.shelfeed.backend.domain.report.dto.request.CreateReportRequest;
import com.shelfeed.backend.domain.report.dto.response.CreateReportResponse;
import com.shelfeed.backend.domain.report.service.ReportService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    `@ResponseStatus`(HttpStatus.CREATED)
    public ApiResponse<CreateReportResponse> createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReportRequest request
    ) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(201, "신고가 접수되었습니다.",
                reportService.createReport(memberUserId, request));
    }
}

