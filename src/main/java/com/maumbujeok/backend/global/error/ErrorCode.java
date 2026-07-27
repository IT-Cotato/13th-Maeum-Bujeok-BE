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
    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "AUTH_004", "이미 가입된 전화번호입니다."),

    // Member
    INVALID_NOTIFICATION_SETTINGS(HttpStatus.BAD_REQUEST, "MEMBER_001", "알림 설정 값이 올바르지 않습니다."),
    INVALID_NOTIFICATION_DAYS(HttpStatus.BAD_REQUEST, "MEMBER_002", "요일별 알림 설정 값이 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "유효하지 않거나 만료된 Refresh Token입니다."),
    PHONE_NUMBER_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_006", "회원 정보의 전화번호와 일치하지 않습니다."),
    SOCIAL_USER_CANNOT_RESET_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_008", "소셜 로그인 가입자는 비밀번호를 재설정할 수 없습니다."),
    SOCIAL_USER_MUST_LOGIN_WITH_OAUTH(HttpStatus.BAD_REQUEST, "AUTH_011", "소셜 로그인으로 가입된 계정입니다. 구글 로그인을 이용해 주세요."),
    
    // SMS Auth
    SMS_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_001", "인증번호가 존재하지 않거나 일치하지 않습니다."),
    SMS_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_002", "만료된 인증번호입니다."),
    SMS_CODE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SMS_003", "전화번호 인증이 완료되지 않았습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_004", "인증번호 전송에 실패했습니다."),
    
    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
