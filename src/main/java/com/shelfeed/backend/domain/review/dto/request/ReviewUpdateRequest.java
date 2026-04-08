package com.shelfeed.backend.domain.review.dto.request;

import com.shelfeed.backend.domain.review.enums.ReviewStatus;
import com.shelfeed.backend.domain.review.enums.ReviewVisibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewUpdateRequest {

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1~5 사이여야 합니다.")
    @Max(value = 5, message = "평점은 1~5 사이여야 합니다.")
    private Byte rating;

    private String content;

    private String quote;

    private Integer readPages;

    private boolean isSpoiler;
    @NotNull(message = "공개 범위는 필수입니다.")
    private ReviewVisibility reviewVisibility;

    @NotNull(message = "감상 상태는 필수입니다.")
    private ReviewStatus reviewStatus;

    @Size(max = 5, message = "태그는 최대 5개까지 등록할 수 있습니다.")
    private List<String> tags;
}
