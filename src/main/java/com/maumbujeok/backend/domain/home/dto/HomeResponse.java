package com.maumbujeok.backend.domain.home.dto;

import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "통합 홈 화면 응답 DTO")
public record HomeResponse(
        @Schema(description = "사용자 이름", example = "김코테")
        String memberName,

        @Schema(description = "오늘 일기 작성 여부", example = "true")
        boolean isDiaryWrittenToday,

        @Schema(description = "오늘의 홈 요약 (사주/오행 기반)")
        HomeSummaryResponse todaySummary,

        @Schema(description = "최근 활성화된 부적 정보 (없을 경우 null)")
        TalismanItemResponse activeTalisman,

        @Schema(description = "오늘의 가이드 문구 (일기 작성/살풀이 팁 등)")
        String todayPromptGuide
) {
}
