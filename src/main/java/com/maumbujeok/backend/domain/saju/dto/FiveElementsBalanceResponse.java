package com.maumbujeok.backend.domain.saju.dto;

import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오행 비율 정보")
public record FiveElementsBalanceResponse(
        @Schema(description = "목 비율", example = "18") Integer wood,
        @Schema(description = "화 비율", example = "22") Integer fire,
        @Schema(description = "토 비율", example = "20") Integer earth,
        @Schema(description = "금 비율", example = "17") Integer metal,
        @Schema(description = "수 비율", example = "23") Integer water
) {
    public static FiveElementsBalanceResponse from(SajuAnalysis analysis) {
        if (analysis.getWoodPercentage() == null
                || analysis.getFirePercentage() == null
                || analysis.getEarthPercentage() == null
                || analysis.getMetalPercentage() == null
                || analysis.getWaterPercentage() == null) {
            return null;
        }
        return new FiveElementsBalanceResponse(
                analysis.getWoodPercentage(),
                analysis.getFirePercentage(),
                analysis.getEarthPercentage(),
                analysis.getMetalPercentage(),
                analysis.getWaterPercentage()
        );
    }
}
