package com.maumbujeok.backend.domain.home.dto;

import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "홈 요약 AI 분석 응답 정보")
public record HomeSummaryAiResponse(
        @Schema(description = "오늘의 행운 팁", example = "따뜻한 물 한 잔 마시기")
        String luckyItem,

        @Schema(description = "사주 기반 오늘의 기운 설명", example = "차분하고 평온하게 마음을 돌보기에 적합한 하루입니다. 조급해하지 말고 나만의 페이스를 유지해보세요.")
        String dailyEnergy,

        @Schema(description = "오늘의 추천 감정", example = "COMFORT")
        ReportEmotion expectedEmotion
) {
    public static HomeSummaryAiResponse createFallback() {
        return new HomeSummaryAiResponse(
                "오늘 하루 따뜻한 물 한 잔 마시기",
                "차분하고 평온하게 마음을 돌보기에 적합한 하루입니다. 조급해하지 말고 나만의 페이스를 유지해보세요.",
                ReportEmotion.COMFORT
        );
    }
}
