package com.maumbujeok.backend.domain.burn.controller;
import com.maumbujeok.backend.domain.burn.application.BurningRequestException;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.error.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes=BurningController.class) @Order(Ordered.HIGHEST_PRECEDENCE)
public class BurningExceptionHandler { @ExceptionHandler(BurningRequestException.class) public ResponseEntity<ApiResponse<Void>> handle(BurningRequestException e){ ErrorCode c=e.getErrorCode(); return ResponseEntity.status(c.getHttpStatus()).body(ApiResponse.onFailure(c.getCode(),c.getMessage(),null)); } }
