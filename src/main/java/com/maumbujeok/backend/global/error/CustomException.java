// src/main/java/com/maumbujeok/backend/global/error/CustomException.java
package com.maumbujeok.backend.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    
    // 에러 발생 시 핸들러에 전달할 예외 정보
    private final ErrorCode errorCode;
}