package com.maumbujeok.backend.domain.talisman.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "부적 개별 정보 응답")
public record TalismanItemResponse(
        @Schema(description = "부적 ID", example = "12")
        Long talismanId,

        @Schema(description = "부적 소각 의식 ID (소각되지 않은 경우 null)", example = "3")
        Long burnRitualId,

        @Schema(description = "부적 디자인 타입", example = "COMFORT_TYPE")
        String designType,

        @Schema(description = "부적 제목", example = "마음의 평온을 주는 부적")
        String title,

        @Schema(description = "부적 메시지 (설명)", example = "이 부적은 불안을 잠재우고 내면에 평화를 깃들게 해 줄 것입니다.")
        String message,

        @Schema(description = "생성 완료된 부적 이미지 URL (생성 중이거나 실패 시 null)", example = "https://object-storage.com/talisman/image.png")
        String imageUrl,

        @Schema(description = "부적 생성 시 활용된 사주 키워드", example = "木 (나무의 기운)")
        String usedSaju,

        @Schema(description = "부적 이미지 생성 상태 [경우의 수 및 프론트엔드 대응 가이드] -> "
                + "1) PROCESSING: AI 부적 이미지 생성 중 (화면에 로딩 스피너 및 대기 화면 노출) "
                + "2) COMPLETED: AI 생성 완료 (화면에 부적 결과 이미지 및 메시지 노출) "
                + "3) FAILED: AI 생성 실패 (화면에 재시도 버튼 및 에러 안내 노출)", example = "COMPLETED")
        TalismanGenerationStatus generationStatus,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @Schema(description = "부적 레코드 생성 일시", example = "2026-08-04T00:25:21+09:00")
        OffsetDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "부적이 속한 날짜", example = "2026-08-04")
        java.time.LocalDate recordedAt
) {
    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.ofHours(9);

    public static TalismanItemResponse from(Talisman talisman) {
        return new TalismanItemResponse(
                talisman.getId(),
                talisman.getBurnRitualId(),
                talisman.getDesignType(),
                talisman.getTitle(),
                talisman.getMessage(),
                talisman.getImageUrl(),
                talisman.getUsedSaju(),
                talisman.getGenerationStatus(),
                talisman.getCreatedAt() == null ? null : talisman.getCreatedAt().atOffset(SEOUL_OFFSET),
                talisman.getRecordedAt()
        );
    }
}
