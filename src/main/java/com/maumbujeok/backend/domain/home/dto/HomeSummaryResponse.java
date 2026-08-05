package com.maumbujeok.backend.domain.home.dto;

import com.maumbujeok.backend.domain.home.domain.HomeSummary;
import com.maumbujeok.backend.domain.home.domain.PrimaryElement;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "홈 화면 요약 응답 DTO")
public record HomeSummaryResponse(
        @Schema(description = "오늘의 오행 기운 (WOOD, FIRE, EARTH, METAL, WATER)", example = "FIRE")
        PrimaryElement primaryElement,

        @Schema(description = "오늘의 행운 팁 (1문장)", example = "오늘은 주변 동료에게 가벼운 안부를 전하며 따뜻한 온기를 나누어 보세요.")
        String todayLuck,

        @Schema(description = "오행 기반 감정 상태 분석 (2문장)", example = "오늘 당신의 사주에는 열정적인 화(火)의 기운이 솟구쳐 새로운 도전에 자신감이 가득합니다. 다만 과도한 의욕으로 촉박해질 수 있으니 차분히 한 호흡 고르고 진행하는 것이 좋습니다.")
        String todayEnergy
) {
    public static HomeSummaryResponse from(HomeSummary entity) {
        return new HomeSummaryResponse(
                entity.getPrimaryElement(),
                entity.getTodayLuck(),
                entity.getTodayEnergy()
        );
    }
}
