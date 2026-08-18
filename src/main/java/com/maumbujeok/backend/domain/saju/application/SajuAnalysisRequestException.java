package com.maumbujeok.backend.domain.saju.application;

import com.maumbujeok.backend.global.error.ErrorCode;

public class SajuAnalysisRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public SajuAnalysisRequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
