package com.shelfeed.backend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class EmailVerifyRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "인증 코드를 입력해주세요.")
    @Pattern(regexp = "^\\d{6}$", message = "6자리 숫자 코드를 입력해주세요.")
    private String code;
}
