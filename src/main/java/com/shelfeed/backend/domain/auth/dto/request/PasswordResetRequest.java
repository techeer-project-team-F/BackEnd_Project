package com.shelfeed.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordResetRequest {
    @NotBlank(message = "재설정 토큰을 입력해주세요.")
    private String token;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문+숫자+특수문자를 포함해야 합니다."
    )
    private String newPassword;
}
