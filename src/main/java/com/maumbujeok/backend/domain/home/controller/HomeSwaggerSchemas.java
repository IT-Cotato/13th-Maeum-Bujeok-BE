package com.maumbujeok.backend.domain.home.controller;

import com.maumbujeok.backend.domain.home.dto.HomeSummaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class HomeSwaggerSchemas {
    private HomeSwaggerSchemas() {
    }

    @Schema(name = "HomeSummaryApiResponse", description = "홈 화면 요약 조회 성공 응답")
    static final class HomeSummaryApiResponse extends ApiResponse<HomeSummaryResponse> {
        private HomeSummaryApiResponse() {
            super(true, "200", "요청이 성공했습니다.", null);
        }
    }

    @Schema(name = "HomeErrorApiResponse", description = "홈 API 에러 공통 응답")
    static final class HomeErrorApiResponse extends ApiResponse<Void> {
        private HomeErrorApiResponse() {
            super(false, "HOME_400", "홈 화면 요약을 조회할 수 없습니다.", null);
        }
    }
}
