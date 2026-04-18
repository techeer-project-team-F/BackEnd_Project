package com.shelfeed.backend.domain.report.dto.response;

import com.shelfeed.backend.domain.report.entity.Report;
import com.shelfeed.backend.domain.report.enums.ReportReason;
import com.shelfeed.backend.domain.report.enums.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long reportId;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private LocalDateTime createdAt;

    public static CreateReportResponse of(Report report, ReportTargetType targetType, Long targetId) {
        return CreateReportResponse.builder()
                .reportId(report.getReportId())
                .targetType(targetType)
                .targetId(targetId)
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .build();
    }
}

