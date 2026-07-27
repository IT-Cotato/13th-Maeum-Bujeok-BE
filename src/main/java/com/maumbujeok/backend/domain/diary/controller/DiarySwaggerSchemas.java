package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

final class DiarySwaggerSchemas {
    private DiarySwaggerSchemas() {
    }

    @Schema(name = "CreateDiaryApiResponse", description = "일기 작성 공통 응답")
    static final class CreateDiaryApiResponse extends ApiResponse<CreateDiaryResponse> {
        private CreateDiaryApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }

    @Schema(name = "DiaryListApiResponse", description = "일기 목록 조회 공통 응답")
    static final class DiaryListApiResponse extends ApiResponse<List<DiaryResponse>> {
        private DiaryListApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }

    @Schema(name = "DiaryAnalysisApiResponse", description = "일기 분석 조회 공통 응답")
    static final class DiaryAnalysisApiResponse extends ApiResponse<DiaryAnalysisResponse> {
        private DiaryAnalysisApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }
}
