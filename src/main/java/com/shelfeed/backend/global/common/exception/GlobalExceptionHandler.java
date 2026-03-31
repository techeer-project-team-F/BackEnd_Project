package com.shelfeed.backend.global.common.exception;

import com.shelfeed.backend.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice//애플리케이션 전체 예외 한 곳에 처리
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리 (우리가 직접 던지는 예외)
    @ExceptionHandler(BusinessException.class)//어떤 종류의 예외를 처리할지 지정
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage()));
    }

    // @Valid 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(400)
                .body(ApiResponse.fail(message));
    }

    // 그 외 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail("서버 내부 오류가 발생했습니다."));
    }
}
