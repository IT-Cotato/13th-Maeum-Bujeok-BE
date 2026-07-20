package com.maumbujeok.backend.domain.diary.application;

public class DiaryRequestException extends RuntimeException {
    private final String code;

    public DiaryRequestException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
