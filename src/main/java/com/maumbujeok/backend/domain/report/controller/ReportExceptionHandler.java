package com.maumbujeok.backend.domain.report.controller;

import com.maumbujeok.backend.domain.report.application.ReportRequestException;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.error.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ReportController.class)
public class ReportExceptionHandler {
    @ExceptionHandler(ReportRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handle(ReportRequestException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }
}
