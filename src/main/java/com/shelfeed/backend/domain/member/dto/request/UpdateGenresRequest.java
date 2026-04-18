package com.shelfeed.backend.domain.member.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateGenresRequest {
    @NotEmpty(message = "최소 1개 이상의 장르를 선택해주세요.")
    private List<Long> genreIds;
}

