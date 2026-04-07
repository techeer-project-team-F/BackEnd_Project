package com.shelfeed.backend.global.common.response;
//모든 API 응답을 status/code/message/data 구조로 만들어 놓은 파일
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON에서 제외
public class ApiResponse<T> {

    private final String status;
    private final int code;
    private final String message;
    private final T data;
    private final List<FieldError> errors;

    // 성공 (쓰기) - message + data 포함
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    // 성공 (조회) - data만 포함
    public static <T> ApiResponse<T> success(int code, T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .code(code)
                .data(data)
                .build();
    }

    // 성공 (data 없음) - message만 포함
    public static ApiResponse<Void> success(int code, String message) {
        return ApiResponse.<Void>builder()
                .status("SUCCESS")
                .code(code)
                .message(message)
                .build();
    }

    // 에러 (필드 검증 실패) - errors 포함
    public static ApiResponse<Void> error(int code, String message, List<FieldError> errors) {
        return ApiResponse.<Void>builder()
                .status("ERROR")
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }

    // 에러 (일반) - message만 포함
    public static ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder()
                .status("ERROR")
                .code(code)
                .message(message)
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
