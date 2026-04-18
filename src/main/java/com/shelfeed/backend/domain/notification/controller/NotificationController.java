package com.shelfeed.backend.domain.notification.controller;

import com.shelfeed.backend.domain.notification.dto.response.NotificationListResponse;
import com.shelfeed.backend.domain.notification.service.NotificationService;
import com.shelfeed.backend.global.common.response.ApiResponse;
import com.shelfeed.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회  GET /api/v1/notifications?cursor=...&limit=20
    @GetMapping
    public ApiResponse<NotificationListResponse> getMyNotifications(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberUserId = userDetails.getMember().getMemberUserId();
        return ApiResponse.success(200,
                notificationService.getMyNotifications(memberUserId, cursor, limit));
    }
}

