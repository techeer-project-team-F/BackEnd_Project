package com.shelfeed.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalMembers;
    private long pendingReports;
    private long totalReports;

    public static AdminDashboardResponse of(long totalMembers, long pendingReports, long totalReports) {
        return AdminDashboardResponse.builder()
                .totalMembers(totalMembers)
                .pendingReports(pendingReports)
                .totalReports(totalReports)
                .build();
    }
}
