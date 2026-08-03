package com.maumbujeok.backend.domain.talisman.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(name = "TalismanItemResponse", description = "\uBD80\uC801 \uBAA9\uB85D \uD56D\uBAA9")
public record TalismanItemResponse(
        @Schema(description = "\uBD80\uC801 ID", example = "101") Long talismanId,
        @Schema(description = "\uC18C\uAC01 \uAE30\uB85D ID", example = "17") Long burnRitualId,
        @Schema(description = "\uBD80\uC801 \uB514\uC790\uC778 \uC720\uD615", example = "CALM", nullable = true) String designType,
        @Schema(description = "\uBD80\uC801 \uC81C\uBAA9", example = "\uB9C8\uC74C\uC744 \uACE0\uC694\uD558\uAC8C \uD558\uB294 \uBD80\uC801", nullable = true) String title,
        @Schema(description = "\uBD80\uC801 \uBA54\uC2DC\uC9C0", example = "\uC774\uC81C\uB294 \uB9C8\uC74C\uC744 \uB193\uC544\uC8FC\uC138\uC694.", nullable = true) String message,
        @Schema(description = "\uBD80\uC801 \uC774\uBBF8\uC9C0 URL", example = "https://cdn.example.com/talismans/101.png", nullable = true) String imageUrl,
        @Schema(description = "\uC0AC\uC6A9\uD55C \uC0AC\uC8FC \uC815\uBCF4", example = "\uC0AC\uC8FC \uC815\uBCF4", nullable = true) String usedSaju,
        @Schema(description = "\uBD80\uC801 \uC0DD\uC131 \uC0C1\uD0DC", example = "COMPLETED") TalismanGenerationStatus generationStatus,
        @Schema(description = "\uBD80\uC801 \uC0DD\uC131 \uC2DC\uAC01", example = "2026-08-03T12:00:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime createdAt,
        @Schema(description = "\uC5F0\uACB0\uB41C \uC77C\uAE30\uC758 \uAE30\uB85D\uC77C", example = "2026-08-03")
        @JsonFormat(pattern = "yyyy-MM-dd") java.time.LocalDate recordedAt
) {
    private static final ZoneOffset SEOUL_OFFSET = ZoneOffset.ofHours(9);

    public static TalismanItemResponse from(Talisman talisman) {
        return new TalismanItemResponse(
                talisman.getId(), talisman.getBurnRitualId(), talisman.getDesignType(), talisman.getTitle(),
                talisman.getMessage(), talisman.getImageUrl(), talisman.getUsedSaju(), talisman.getGenerationStatus(),
                talisman.getCreatedAt() == null ? null : talisman.getCreatedAt().atOffset(SEOUL_OFFSET),
                talisman.getRecordedAt()
        );
    }
}