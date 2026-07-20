package com.maumbujeok.backend.domain.diary.ai;

public class AiAnalysisException extends RuntimeException {
    private final String failureCode;
    private final int attempts;

    public AiAnalysisException(String failureCode, int attempts, Throwable cause) {
        super(failureCode, cause);
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
