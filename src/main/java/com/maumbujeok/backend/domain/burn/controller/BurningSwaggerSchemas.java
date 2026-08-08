package com.maumbujeok.backend.domain.burn.controller;

import com.maumbujeok.backend.domain.burn.dto.BurningAnalysisResponse;
import com.maumbujeok.backend.domain.burn.dto.BurningDetailResponse;
import com.maumbujeok.backend.domain.burn.dto.BurningListResponse;
import com.maumbujeok.backend.domain.burn.dto.CreateBurningResponse;
import com.maumbujeok.backend.domain.burn.dto.TalismanCreationResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class BurningSwaggerSchemas {
    private BurningSwaggerSchemas() {}

    @Schema(name = "CreateBurningApiResponse", description = "소각 시작 공통 응답")
    static final class CreateBurningApiResponse extends ApiResponse<CreateBurningResponse> {
        private CreateBurningApiResponse() { super(true, "200", "요청이 성공했습니다.", null); }
    }
    @Schema(name = "BurningListApiResponse", description = "소각 목록 공통 응답")
    static final class BurningListApiResponse extends ApiResponse<BurningListResponse> {
        private BurningListApiResponse() { super(true, "200", "요청이 성공했습니다.", null); }
    }
    @Schema(name = "BurningDetailApiResponse", description = "소각 상세 공통 응답")
    static final class BurningDetailApiResponse extends ApiResponse<BurningDetailResponse> {
        private BurningDetailApiResponse() { super(true, "200", "요청이 성공했습니다.", null); }
    }
    @Schema(name = "BurningAnalysisApiResponse", description = "소각 분석 공통 응답")
    static final class BurningAnalysisApiResponse extends ApiResponse<BurningAnalysisResponse> {
        private BurningAnalysisApiResponse() { super(true, "200", "요청이 성공했습니다.", null); }
    }
    @Schema(name = "TalismanCreationApiResponse", description = "부적 생성 공통 응답")
    static final class TalismanCreationApiResponse extends ApiResponse<TalismanCreationResponse> {
        private TalismanCreationApiResponse() { super(true, "200", "요청이 성공했습니다.", null); }
    }
    @Schema(name = "BurningErrorApiResponse", description = "소각 API 오류 공통 응답")
    static final class BurningErrorApiResponse extends ApiResponse<Void> {
        private BurningErrorApiResponse() { super(false, "BURN_400", "소각 요청이 잘못되었습니다.", null); }
    }
}
