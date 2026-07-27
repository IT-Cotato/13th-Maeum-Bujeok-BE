package com.maumbujeok.backend.domain.report.controller;

import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportSummaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class ReportSwaggerSchemas {
    private ReportSwaggerSchemas() {
    }

    @Schema(name = "GenerateWeeklyReportApiResponse", description = "주간 감정 리포트 생성 공통 응답")
    static final class GenerateWeeklyReportApiResponse extends ApiResponse<GenerateWeeklyReportResponse> {
        private GenerateWeeklyReportApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }

    @Schema(name = "WeeklyReportSummaryApiResponse", description = "주간 리포트 요약 조회 공통 응답")
    static final class WeeklyReportSummaryApiResponse extends ApiResponse<WeeklyReportSummaryResponse> {
        private WeeklyReportSummaryApiResponse() {
            super(true, "200", "요청에 성공하였습니다.", null);
        }
    }
}
