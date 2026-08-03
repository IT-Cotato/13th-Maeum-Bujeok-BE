package com.maumbujeok.backend.domain.talisman.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "부적 목록 페이징 응답 정보")
public record TalismanListResponse(
        @Schema(description = "조회된 부적 목록")
        List<TalismanItemResponse> items,

        @Schema(description = "현재 페이지에 포함된 부적 개수", example = "3")
        int count,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "다음 페이지 조회를 위한 커서 ID (더 이상 없으면 null)", example = "9")
        Long nextCursor
) {
}
