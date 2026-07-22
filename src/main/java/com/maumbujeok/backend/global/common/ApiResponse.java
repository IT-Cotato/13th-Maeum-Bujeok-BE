// src/main/java/com/maumbujeok/backend/global/common/ApiResponse.java
package com.maumbujeok.backend.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "ApiResponse", description = "모든 API에서 사용하는 공통 응답 형식")
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean success;

    @Schema(description = "성공 시 200, 실패 시 도메인 에러코드", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String code;

    @Schema(description = "사용자에게 표시 가능한 응답 메시지", example = "요청에 성공하였습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String message;

    @Schema(description = "응답 데이터. 실패 응답에서는 null")
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
