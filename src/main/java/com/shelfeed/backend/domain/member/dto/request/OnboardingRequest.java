package com.shelfeed.backend.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class OnboardingRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요.")
    private String nickname;

    private String profileImageUrl;

    @Size(max = 300, message = "소개는 300자 이내로 입력해주세요.")
    private String bio;

    @NotEmpty(message = "최소 1개 이상의 장르를 선택해주세요.")
    private List<Long> genreIds;
}
