package com.maumbujeok.backend.domain.talisman.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import com.maumbujeok.backend.global.util.TimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "TalismanItemResponse", description = "부적 목록 항목")
public record TalismanItemResponse(
        @Schema(description = "부적 ID", example = "101") Long talismanId,
        @Schema(description = "소각 기록 ID", example = "17") Long burnRitualId,
        @Schema(description = "부적 디자인 유형", example = "CALM", nullable = true) String designType,
        @Schema(description = "부적 제목", example = "마음을 고요하게 하는 부적", nullable = true) String title,
        @Schema(description = "부적 메시지", example = "이제는 마음을 놓아주세요.", nullable = true) String message,
        @Schema(description = "부적 이미지 URL", example = "https://cdn.example.com/talismans/101.png", nullable = true) String imageUrl,
        @Schema(description = "사용한 사주 정보", example = "사주 정보", nullable = true) String usedSaju,
        @Schema(description = "부적 생성 상태", example = "COMPLETED") TalismanGenerationStatus generationStatus,
        @Schema(description = "부적 생성 시각", example = "2026-08-03T12:00:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime createdAt,
        @Schema(description = "연결된 일기의 기록일", example = "2026-08-03")
        @JsonFormat(pattern = "yyyy-MM-dd") java.time.LocalDate recordedAt
) {
    public static TalismanItemResponse from(Talisman talisman) {
        return new TalismanItemResponse(
                talisman.getId(), talisman.getBurnRitualId(), talisman.getDesignType(), talisman.getTitle(),
                talisman.getMessage(), talisman.getImageUrl(), talisman.getUsedSaju(), talisman.getGenerationStatus(),
                TimeUtils.toSeoulOffset(talisman.getCreatedAt()),
                talisman.getRecordedAt()
        );
    }
}