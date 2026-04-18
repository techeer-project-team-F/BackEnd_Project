package com.shelfeed.backend.domain.report.dto.request;

import com.shelfeed.backend.domain.report.enums.ReportReason;
import com.shelfeed.backend.domain.report.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateReportRequest {

    @NotNull(message = "targetType은 필수입니다.")
    private ReportTargetType targetType;

    @NotNull(message = "targetId는 필수입니다.")
    private Long targetId;

    @NotNull(message = "reason은 필수입니다.")
    private ReportReason reason;

    @Size(max = 200, message = "description은 200자 이내로 입력해주세요.")
    private String description;
}

