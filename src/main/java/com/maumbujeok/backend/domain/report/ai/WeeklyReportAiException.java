package com.maumbujeok.backend.domain.report.ai;

import lombok.Getter;

@Getter
public class WeeklyReportAiException extends RuntimeException {
    private final String failureCode;
    private final int attempts;

    public WeeklyReportAiException(String failureCode, int attempts) {
        this.failureCode = failureCode;
        this.attempts = attempts;
    }

    public WeeklyReportAiException(String failureCode, int attempts, Throwable cause) {
        super(cause);
        this.failureCode = failureCode;
        this.attempts = attempts;
    }
}
