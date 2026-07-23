package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.global.error.ErrorCode;

public class DiaryRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public DiaryRequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
