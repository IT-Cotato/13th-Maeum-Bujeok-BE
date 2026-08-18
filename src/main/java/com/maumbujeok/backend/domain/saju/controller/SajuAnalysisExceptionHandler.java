package com.maumbujeok.backend.domain.saju.controller;

import com.maumbujeok.backend.domain.saju.application.SajuAnalysisRequestException;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.error.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = SajuAnalysisController.class)
public class SajuAnalysisExceptionHandler {
    @ExceptionHandler(SajuAnalysisRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handle(SajuAnalysisRequestException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        String message = exception.getMessage() != null ? exception.getMessage() : errorCode.getMessage();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), message, null));
    }
}
