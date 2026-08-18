package com.maumbujeok.backend.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "이용약관 상세 정보")
public record TermsDetailResponse(
        String termType,
        String title,
        boolean required,
        String version,
        LocalDate effectiveDate,
        @Schema(description = "약관 전문") String content
) {
}
