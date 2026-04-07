package com.shelfeed.backend.domain.member.dto.request;

import lombok.Getter;

@Getter
public class WithdrawRequest {
    private String password;

    private String reason;

}
