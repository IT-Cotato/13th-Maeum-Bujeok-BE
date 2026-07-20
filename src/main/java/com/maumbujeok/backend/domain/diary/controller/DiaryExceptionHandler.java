package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.application.DiaryRequestException;
import com.maumbujeok.backend.global.common.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = DiaryController.class)
public class DiaryExceptionHandler {
    @ExceptionHandler(DiaryRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handle(DiaryRequestException exception) {
        HttpStatus status = "DIARY_404".equals(exception.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiResponse.onFailure(exception.getCode(), exception.getMessage(), null));
    }
}
