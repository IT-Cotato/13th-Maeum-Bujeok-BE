package com.maumbujeok.backend.global.ai.exception;

import lombok.Getter;

@Getter
public class AiClientException extends RuntimeException {
    private final AiFailureCode failureCode;
    private final int attempts;

    public AiClientException(AiFailureCode failureCode, int attempts, Throwable cause) {
        super(failureCode.name(), cause);
        this.failureCode = failureCode;
        this.attempts = attempts;
    }

    public AiClientException(AiFailureCode failureCode, int attempts) {
        this(failureCode, attempts, null);
    }
}
