package com.maumbujeok.backend.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "이용약관 목록 항목")
public record TermsSummaryResponse(
        @Schema(description = "약관 유형", example = "SERVICE_TERMS") String termType,
        @Schema(description = "약관 제목", example = "서비스 이용약관") String title,
        @Schema(description = "필수 동의 여부", example = "true") boolean required,
        @Schema(description = "약관 버전", example = "v1.0") String version,
        @Schema(description = "시행일. 미정이면 null", nullable = true) LocalDate effectiveDate
) {
}
