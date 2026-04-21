package com.shelfeed.backend.domain.member.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class WithdrawRequest {

    @Size(max = 100, message = "비밀번호는 100자를 초과할 수 없습니다.")
    private String password;

    @Size(max = 500, message = "탈퇴 사유는 500자를 초과할 수 없습니다.")
    private String reason;
}
