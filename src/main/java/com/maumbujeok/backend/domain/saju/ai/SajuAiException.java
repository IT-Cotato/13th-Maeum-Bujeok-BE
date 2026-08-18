package com.maumbujeok.backend.domain.saju.ai;

public class SajuAiException extends RuntimeException {
    private final String failureCode;
    private final int attempts;

    public SajuAiException(String failureCode, int attempts, Throwable cause) {
        super(cause);
        this.failureCode = failureCode;
        this.attempts = attempts;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public int getAttempts() {
        return attempts;
    }
}
