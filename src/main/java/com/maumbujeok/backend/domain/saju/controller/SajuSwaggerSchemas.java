package com.maumbujeok.backend.domain.saju.controller;

import com.maumbujeok.backend.domain.saju.dto.CreateSajuAnalysisResponse;
import com.maumbujeok.backend.domain.saju.dto.SajuAnalysisResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class SajuSwaggerSchemas {
    private SajuSwaggerSchemas() {
    }

    @Schema(name = "CreateSajuAnalysisApiResponse", description = "사주 분석 요청 생성 성공 응답")
    static final class CreateSajuAnalysisApiResponse extends ApiResponse<CreateSajuAnalysisResponse> {
        private CreateSajuAnalysisApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }

    @Schema(name = "SajuAnalysisApiResponse", description = "사주 분석 상태 조회 성공 응답")
    static final class SajuAnalysisApiResponse extends ApiResponse<SajuAnalysisResponse> {
        private SajuAnalysisApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }

    @Schema(name = "SajuAnalysisErrorApiResponse", description = "사주 분석 요청 실패 응답")
    static final class SajuAnalysisErrorApiResponse extends ApiResponse<Void> {
        private SajuAnalysisErrorApiResponse() {
            super(false, "SAJU_400", "사주 분석 요청이 올바르지 않습니다.", null);
        }
    }
}
