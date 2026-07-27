package com.maumbujeok.backend.domain.talisman.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record TalismanItemResponse(
        Long talismanId,
        Long burnRitualId,
        String designType,
        String title,
        String message,
        String imageUrl,
        String usedSaju,
        TalismanGenerationStatus generationStatus,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt
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
                talisman.getCreatedAt() == null ? null : talisman.getCreatedAt().atOffset(SEOUL_OFFSET)
        );
    }
}
