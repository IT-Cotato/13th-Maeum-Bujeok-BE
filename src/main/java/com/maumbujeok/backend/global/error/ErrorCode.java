// src/main/java/com/maumbujeok/backend/global/error/ErrorCode.java
package com.maumbujeok.backend.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    // Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_002", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "AUTH_003", "이미 존재하는 아이디입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "AUTH_004", "이미 가입된 전화번호입니다."),
    
    // SMS Auth
    SMS_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_001", "인증번호가 존재하지 않거나 일치하지 않습니다."),
    SMS_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_002", "만료된 인증번호입니다."),
    SMS_CODE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SMS_003", "전화번호 인증이 완료되지 않았습니다."),
    
    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}