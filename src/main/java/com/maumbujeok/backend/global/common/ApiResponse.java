// src/main/java/com/maumbujeok/backend/global/common/ApiResponse.java
package com.maumbujeok.backend.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    
    private final boolean isSuccess;
    private final String code;
    private final String message;
    private final T data;

    // 성공 응답 팩토리 메서드
    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(true, "200", "요청에 성공하였습니다.", data);
    }

    // 실패 응답 팩토리 메서드
    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}