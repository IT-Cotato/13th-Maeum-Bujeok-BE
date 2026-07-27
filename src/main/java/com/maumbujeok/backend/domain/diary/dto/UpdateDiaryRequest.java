package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateDiaryRequest", description = "일기 수정 요청. 기록일은 변경하지 않습니다.")
public record UpdateDiaryRequest(
        @Schema(description = "수정할 일기 내용 (1~5000자)", example = "오늘의 마음을 다시 정리해 보았다.", nullable = true)
        String content,
        @Schema(description = "수정할 선택 감정 코드", example = "COMFORTABLE", nullable = true)
        String selectedEmotion
) {
}
