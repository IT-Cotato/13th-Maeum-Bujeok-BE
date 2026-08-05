package com.maumbujeok.backend.domain.report.controller;

final class ReportSwaggerExamples {
    private ReportSwaggerExamples() {
    }

    static final String GENERATE_WEEKLY_SUCCESS = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "emotionReportId": 12,
                "reportType": "WEEKLY",
                "periodStart": "2026-07-13",
                "periodEnd": "2026-07-19",
                "generationStatus": "PENDING",
                "message": "주간 감정 리포트 생성을 시작했습니다."
              }
            }
            """;

    static final String WEEKLY_SUMMARY_COMPLETED = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "summaryId": 12,
                "emotionReportId": 12,
                "periodStart": "2026-07-13",
                "periodEnd": "2026-07-19",
                "generationStatus": "COMPLETED",
                "insightSummary": "이번 주는 마음님에게 꽤 괜찮은 한 주였어요!\\n\\n이번 주 마음님의 기록에는 따뜻한 기운이 가득했어요.\\n\\n66.7%가 긍정적인 감정으로 채워진 한 주였네요. 작은 일상 속에서 스스로를 다독이며 안정감을 회복해 간 흐름이 느껴졌어요.",
                "modelName": "gpt-5.5",
                "reportVersion": "weekly-report-v1",
                "generatedAt": "2026-07-19T23:10:00+09:00"
              }
            }
            """;

    static final String WEEKLY_SUMMARY_PENDING = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "summaryId": 12,
                "emotionReportId": 12,
                "periodStart": "2026-07-13",
                "periodEnd": "2026-07-19",
                "generationStatus": "PENDING",
                "insightSummary": null,
                "modelName": null,
                "reportVersion": "weekly-report-v1",
                "generatedAt": null
              }
            }
            """;

    static final String WEEKLY_SUMMARY_FAILED = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "summaryId": 12,
                "emotionReportId": 12,
                "periodStart": "2026-07-13",
                "periodEnd": "2026-07-19",
                "generationStatus": "FAILED",
                "insightSummary": null,
                "modelName": null,
                "reportVersion": "weekly-report-v1",
                "generatedAt": "2026-07-19T23:10:00+09:00"
              }
            }
            """;

    static final String GENERATE_WEEKLY_WITHOUT_DIARY_SUCCESS = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "emotionReportId": 12,
                "reportType": "WEEKLY",
                "periodStart": "2026-07-13",
                "periodEnd": "2026-07-19",
                "generationStatus": "COMPLETED",
                "message": "작성된 일기가 없어 안내 문구로 주간 감정 리포트를 생성했습니다."
              }
            }
            """;

    static final String WEEKLY_SUMMARY_NOT_FOUND = """
            {
              "success": false,
              "code": "REPORT_404",
              "message": "주간 리포트 요약을 찾을 수 없습니다.",
              "data": null
            }
            """;

    static final String INVALID_WEEK_START = """
            {
              "success": false,
              "code": "REPORT_400",
              "message": "weekStart는 필수입니다.",
              "data": null
            }
            """;

    static final String INTERNAL_SERVER_ERROR = """
            {
              "success": false,
              "code": "COMMON_500",
              "message": "서버 에러가 발생했습니다.",
              "data": null
            }
            """;
}
