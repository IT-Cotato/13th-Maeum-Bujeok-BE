package com.maumbujeok.backend.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_DIARY_CURSOR(HttpStatus.BAD_REQUEST, "DIARY_CURSOR_400", "일기 목록 커서가 올바르지 않습니다."),

    INVALID_UPLOAD_REQUEST(HttpStatus.BAD_REQUEST, "UPLOAD_400", "이미지 업로드 요청이 올바르지 않습니다."),
    UPLOAD_NOT_FOUND(HttpStatus.NOT_FOUND, "UPLOAD_404", "업로드 정보를 찾을 수 없습니다."),
    UPLOAD_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_500", "이미지 저장소 처리 중 오류가 발생했습니다."),

    // Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "회원 정보를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_002", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "AUTH_004", "이미 가입된 전화번호입니다."),
    ALREADY_REGISTERED_PHONE(HttpStatus.CONFLICT, "AUTH_009", "이미 가입된 전화번호입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_010", "가입되지 않은 전화번호입니다."),
    INVALID_SMS_PURPOSE(HttpStatus.BAD_REQUEST, "AUTH_012", "지원하지 않는 SMS 인증 목적입니다."),

    // Member
    INVALID_NOTIFICATION_SETTINGS(HttpStatus.BAD_REQUEST, "MEMBER_001", "알림 설정 값이 올바르지 않습니다."),
    INVALID_NOTIFICATION_DAYS(HttpStatus.BAD_REQUEST, "MEMBER_002", "요일별 알림 설정 값이 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "유효하지 않거나 만료된 Refresh Token입니다."),
    PHONE_NUMBER_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_006", "회원 정보의 전화번호와 일치하지 않습니다."),
    SOCIAL_USER_CANNOT_RESET_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_008", "소셜 로그인 가입자는 비밀번호를 재설정할 수 없습니다."),
    SOCIAL_USER_MUST_LOGIN_WITH_OAUTH(HttpStatus.BAD_REQUEST, "AUTH_011", "소셜 로그인으로 가입된 계정입니다. 구글 로그인을 이용해 주세요."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.CONFLICT, "AUTH_009", "이미 온보딩을 완료한 회원입니다."),

    // Diary
    INVALID_DIARY_REQUEST(HttpStatus.BAD_REQUEST, "DIARY_400", "일기 요청이 올바르지 않습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY_404", "일기를 찾을 수 없습니다."),

    // Burning
    INVALID_BURNING_REQUEST(HttpStatus.BAD_REQUEST, "BURN_400", "소각 요청이 올바르지 않습니다."),
    BURNING_NOT_FOUND(HttpStatus.NOT_FOUND, "BURN_404", "소각 기록을 찾을 수 없습니다."),
    DIARY_ALREADY_BURNED(HttpStatus.CONFLICT, "BURN_409", "이미 소각된 일기입니다."),
    BURNING_ANALYSIS_NOT_COMPLETED(HttpStatus.CONFLICT, "BURN_410", "소각 분석이 아직 완료되지 않았습니다."),
    TALISMAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "TALISMAN_409", "이미 부적이 생성되었습니다."),
    TALISMAN_NOT_FOUND(HttpStatus.NOT_FOUND, "TALISMAN_404", "부적을 찾을 수 없습니다."),

    // Report
    INVALID_REPORT_REQUEST(HttpStatus.BAD_REQUEST, "REPORT_400", "리포트 요청이 올바르지 않습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404", "리포트를 찾을 수 없습니다."),
    FLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW_001", "다음 주 흐름 정보를 찾을 수 없습니다."),

    // SMS Auth
    SMS_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_001", "인증번호를 찾을 수 없습니다."),
    SMS_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_002", "인증번호가 만료되었습니다."),
    SMS_CODE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SMS_003", "인증번호가 확인되지 않았습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_004", "인증번호 전송에 실패했습니다."),

    // Common
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "COMMON_403", "인증이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}