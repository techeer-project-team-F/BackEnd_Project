package com.shelfeed.backend.domain.admin.controller;

import com.shelfeed.backend.domain.admin.dto.response.AdminDashboardResponse;
import com.shelfeed.backend.domain.admin.service.AdminService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // GET /api/v1/admin/dashboard
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        return ApiResponse.success(200, "대시보드 조회 성공", adminService.getDashboard());
    }
}
