package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCalendarResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCursorPageResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryDetailResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

final class DiarySwaggerSchemas {
    private DiarySwaggerSchemas() {
    }

    @Schema(name = "CreateDiaryApiResponse", description = "일기 작성 공통 응답")
    static final class CreateDiaryApiResponse extends ApiResponse<CreateDiaryResponse> {
        private CreateDiaryApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "DiaryCursorPageApiResponse", description = "일기 커서 목록 공통 응답")
    static final class DiaryCursorPageApiResponse extends ApiResponse<DiaryCursorPageResponse> {
        private DiaryCursorPageApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "LegacyDiaryListApiResponse", description = "날짜·연월 필터 일기 목록 공통 응답")
    static final class LegacyDiaryListApiResponse extends ApiResponse<List<DiaryResponse>> {
        private LegacyDiaryListApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "DiaryCalendarApiResponse", description = "월별 일기 달력 공통 응답")
    static final class DiaryCalendarApiResponse extends ApiResponse<DiaryCalendarResponse> {
        private DiaryCalendarApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "DiaryDetailApiResponse", description = "일기 상세 공통 응답")
    static final class DiaryDetailApiResponse extends ApiResponse<DiaryDetailResponse> {
        private DiaryDetailApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "UpdateDiaryApiResponse", description = "일기 수정 공통 응답")
    static final class UpdateDiaryApiResponse extends ApiResponse<UpdateDiaryResponse> {
        private UpdateDiaryApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "DiaryEmotionStatsApiResponse", description = "대표 감정 통계 공통 응답")
    static final class DiaryEmotionStatsApiResponse extends ApiResponse<List<EmotionStatResponse>> {
        private DiaryEmotionStatsApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "DiaryAnalysisApiResponse", description = "일기 분석 조회 공통 응답")
    static final class DiaryAnalysisApiResponse extends ApiResponse<DiaryAnalysisResponse> {
        private DiaryAnalysisApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "DiaryErrorApiResponse", description = "일기 API 오류 공통 응답")
    static final class DiaryErrorApiResponse extends ApiResponse<Void> {
        private DiaryErrorApiResponse() { super(false, "DIARY_400", "일기 요청이 올바르지 않습니다.", null); }
    }
}
