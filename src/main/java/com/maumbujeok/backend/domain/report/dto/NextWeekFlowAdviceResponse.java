package com.maumbujeok.backend.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "다음 주 흐름 AI 조언 상세 정보")
public record NextWeekFlowAdviceResponse(
        @Schema(description = "조언의 요약 제목", example = "차분하고 따뜻한 여유의 한 주")
        String title,
        
        @Schema(description = "조언의 상세 본문 내용", example = "스스로에게 과한 부담감을 주지 마시고 한 박자 쉬어가며, 가까운 이들과 따뜻한 차를 마시며 여유를 느껴보세요.")
        String content,
        
        @Schema(description = "핵심 강조 문구", example = "한 박자 쉬어가기")
        String highlight
) {
}
