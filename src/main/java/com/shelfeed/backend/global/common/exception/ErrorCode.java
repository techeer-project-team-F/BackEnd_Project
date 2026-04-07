package com.shelfeed.backend.global.common.exception;
//에러코드 관리
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
    INVALID_CURRENT_PASSWORD(400, "M004", "현재 비밀번호가 올바르지 않습니다."),
    NO_PASSWORD_ACCOUNT(400, "M005", "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다."),
    //인증
    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
    UNAUTHORIZED(401, "A003", "인증이 필요합니다."),
    INVALID_PASSWORD(401,"A004", "비밀번호가 일치하지 않습니다."),
    WITHDRAWN_MEMBER(403,"A005", "탈퇴한 계정입니다."),
    INVALID_EMAIL_CODE(400,"A006", "인증 코드가 올바르지 않습니다."),
    CODE_EXPIRED(400,"A007", "인증 코드가 만료되었습니다."),
    CODE_ATTEMPTS_EXCEEDED(429,"A008", "인증 시도 횟수를 초과했습니다. 코드를 재발송해주세요."),
    ALREADY_VERIFIED_EMAIL(409,"A009", "이미 인증된 이메일입니다."),
    RESEND_COOLDOWN(429,"A010", "60초 후에 다시 시도해주세요."),
    INVALID_PASSWORD_RESET_TOKEN(400, "A011", "유효하지 않거나 만료된 비밀번호 재설정 토큰입니다."),
    SAME_PASSWORD(400,"A012", "기존 비밀번호와 동일합니다."),
    TOKEN_REUSE_DETECTED(401,"A013", "토큰 재사용이 감지되었습니다. 모든 세션이 로그아웃됩니다."),
    INVALID_OAUTH_STATE(400,"A014", "유효하지 않은 OAuth state입니다."),
    OAUTH_PROVIDER_ERROR(502,"A015", "OAuth 제공자 연동 중 오류가 발생했습니다."),
    //도서
    BOOK_NOT_FOUND(404, "B001", "존재하지 않는 도서입니다."),
    //일반
    INVALID_INPUT(400, "C002", "잘못된 입력값입니다."),
    FORBIDDEN(403, "C001", "접근 권한이 없습니다."),;

    private final int status;
    private final String code;
    private final String message;

}
