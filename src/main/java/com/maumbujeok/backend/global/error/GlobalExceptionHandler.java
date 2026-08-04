// src/main/java/com/maumbujeok/backend/global/error/GlobalExceptionHandler.java
package com.maumbujeok.backend.global.error;

import com.maumbujeok.backend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 발생 시 낚아채서 일관된 포맷으로 반환
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("CustomException: [Code: {}, Message: {}]", errorCode.getCode(), errorCode.getMessage(), e);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    // MethodArgumentNotValidException 처리 (Validation 실패)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String defaultMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("Validation failed: {}", defaultMessage, e);
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("COMMON_400", defaultMessage, null));
    }

    // DB Unique 제약 조건 위반 예외 처리 (동시성 가입 방어용)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        ErrorCode errorCode = ErrorCode.ALREADY_REGISTERED_PHONE;
        log.error("DataIntegrityViolationException: [Code: {}, Message: {}]", errorCode.getCode(), errorCode.getMessage(), e);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    // 예측하지 못한 서버 내부 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("Unhandled Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }
}