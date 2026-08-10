// src/main/java/com/maumbujeok/backend/global/error/GlobalExceptionHandler.java
package com.maumbujeok.backend.global.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.maumbujeok.backend.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalTime;

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
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("COMMON_400", defaultMessage, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = resolveNotReadableMessage(e);
        log.error("Request body parse failed: {}", message, e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("COMMON_400", message, null));
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

    private String resolveNotReadableMessage(HttpMessageNotReadableException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof InvalidFormatException invalidFormatException) {
                String fieldName = invalidFormatException.getPath().isEmpty()
                        ? ""
                        : String.valueOf(invalidFormatException.getPath().get(0).getFieldName());
                Class<?> targetType = invalidFormatException.getTargetType();

                if ("birthTime".equals(fieldName) || LocalTime.class.isAssignableFrom(targetType)) {
                    return "태어난 시간은 HH:mm 형식이어야 합니다.";
                }
                if ("gender".equals(fieldName)) {
                    return "성별은 여성, 남성, 선택안함 또는 FEMALE, MALE, NONE 중 하나여야 합니다.";
                }
                if ("calendarType".equals(fieldName)) {
                    return "달력 유형은 SOLAR, LUNAR, LUNAR_LEAP 중 하나여야 합니다.";
                }
            }

            if (cause instanceof ValueInstantiationException valueInstantiationException) {
                String originalMessage = valueInstantiationException.getOriginalMessage();
                if (originalMessage != null && originalMessage.contains("Unsupported gender")) {
                    return "성별은 여성, 남성, 선택안함 또는 FEMALE, MALE, NONE 중 하나여야 합니다.";
                }
            }

            if (cause instanceof IllegalArgumentException illegalArgumentException) {
                String message = illegalArgumentException.getMessage();
                if (message != null && message.contains("Unsupported gender")) {
                    return "성별은 여성, 남성, 선택안함 또는 FEMALE, MALE, NONE 중 하나여야 합니다.";
                }
            }

            cause = cause.getCause();
        }

        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("LocalTime") || message.contains("birthTime")) {
                return "태어난 시간은 HH:mm 형식이어야 합니다.";
            }
            if (message.contains("Gender") || message.contains("Unsupported gender")) {
                return "성별은 여성, 남성, 선택안함 또는 FEMALE, MALE, NONE 중 하나여야 합니다.";
            }
            if (message.contains("calendarType")) {
                return "달력 유형은 SOLAR, LUNAR, LUNAR_LEAP 중 하나여야 합니다.";
            }
        }

        return "요청 본문 형식이 올바르지 않습니다.";
    }
}
