package com.shelfeed.backend.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {//enum 타입은 public 불가(자동 private) + 객체 생성 시 new 안됨
    //enum 규칙
    //상수 선언이 먼저, 다음 필드, 다음 생성자(어노테이션)
    //
    //멤버
    MEMBER_NOT_FOUND(404, "M001", "존재하지 않는 회원입니다."),
    EMAIL_ALREADY_EXISTS(409, "M002", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(409, "M003", "이미 사용 중인 닉네임입니다."),
    //인증
    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
    UNAUTHORIZED(401, "A003", "인증이 필요합니다."),
    //도서
    BOOK_NOT_FOUND(404, "B001", "존재하지 않는 도서입니다."),
    //일반
    INVALID_INPUT(400, "C002", "잘못된 입력값입니다."),
    FORBIDDEN(403, "C001", "접근 권한이 없습니다."),;

    private final int status;
    private final String code;
    private final String message;

}
