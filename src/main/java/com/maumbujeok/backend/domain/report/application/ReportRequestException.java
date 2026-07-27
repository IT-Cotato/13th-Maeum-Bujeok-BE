package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.global.error.ErrorCode;

public class ReportRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public ReportRequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
