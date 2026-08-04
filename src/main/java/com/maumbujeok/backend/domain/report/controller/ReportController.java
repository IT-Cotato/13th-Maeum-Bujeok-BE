package com.maumbujeok.backend.domain.report.controller;

import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.GENERATE_WEEKLY_SUCCESS;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.GENERATE_WEEKLY_WITHOUT_DIARY_SUCCESS;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.INTERNAL_SERVER_ERROR;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.INVALID_WEEK_START;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.WEEKLY_SUMMARY_COMPLETED;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.WEEKLY_SUMMARY_FAILED;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.WEEKLY_SUMMARY_NOT_FOUND;
import static com.maumbujeok.backend.domain.report.controller.ReportSwaggerExamples.WEEKLY_SUMMARY_PENDING;

import com.maumbujeok.backend.domain.report.application.WeeklyReportService;
import com.maumbujeok.backend.domain.report.application.ReportScreenService;
import com.maumbujeok.backend.domain.report.application.MonthlyReportService;
import com.maumbujeok.backend.domain.report.controller.ReportSwaggerSchemas.GenerateWeeklyReportApiResponse;
import com.maumbujeok.backend.domain.report.controller.ReportSwaggerSchemas.WeeklyReportSummaryApiResponse;
import com.maumbujeok.backend.domain.report.controller.ReportSwaggerSchemas.WeeklyReportPeriodsApiResponse;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportSummaryResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportPeriodResponse;
import com.maumbujeok.backend.domain.report.dto.ReportEmotionStatsResponse;
import com.maumbujeok.backend.domain.report.dto.ReportBurningItemResponse;
import com.maumbujeok.backend.domain.report.dto.ReportTalismansResponse;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowQueryResponse;
import com.maumbujeok.backend.domain.report.dto.GenerateMonthlyReportRequest;
import com.maumbujeok.backend.domain.report.dto.MonthlyReportPeriodResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "리포트 API", description = "주간 감정 리포트 생성 및 조회 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class ReportController {
    private final WeeklyReportService weeklyReportService;
    private final ReportScreenService reportScreenService;
    private final MonthlyReportService monthlyReportService;

    @Operation(
            summary = "주간 감정 리포트 요약 생성",
            description = "해당 주차의 일기와 감정 비율을 바탕으로 주간 리포트 요약 생성을 비동기로 시작합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생성 시작 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GenerateWeeklyReportApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "일기 있음", value = GENERATE_WEEKLY_SUCCESS),
                                    @ExampleObject(name = "일기 없음", value = GENERATE_WEEKLY_WITHOUT_DIARY_SUCCESS)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 오류 (REPORT_400)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "weekStart 누락", value = INVALID_WEEK_START)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (COMMON_500)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = INTERNAL_SERVER_ERROR)
                    )
            )
    })
    @PostMapping("/weekly/generate")
    public ApiResponse<GenerateWeeklyReportResponse> generateWeekly(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "주간 감정 리포트를 생성할 시작일(월요일)"
            )
            @RequestBody GenerateWeeklyReportRequest request
    ) {
        return ApiResponse.onSuccess(
                weeklyReportService.generate(userDetails.getMember().getPhoneNumber(), request)
        );
    }

    @Operation(
            summary = "주간 리포트 요약 결과 조회",
            description = "생성 요청한 주간 리포트 요약의 상태와 결과를 조회합니다. 현재 summaryId는 생성 응답의 emotionReportId와 동일하게 사용합니다. PENDING 또는 PROCESSING이면 생성 중이며, FAILED이면 insightSummary는 null로 반환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WeeklyReportSummaryApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "생성 완료", value = WEEKLY_SUMMARY_COMPLETED),
                                    @ExampleObject(name = "생성 중", value = WEEKLY_SUMMARY_PENDING),
                                    @ExampleObject(name = "생성 실패", value = WEEKLY_SUMMARY_FAILED)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "요약 없음 또는 접근 권한 없음 (REPORT_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = WEEKLY_SUMMARY_NOT_FOUND)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (COMMON_500)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = INTERNAL_SERVER_ERROR)
                    )
            )
    })
    @GetMapping("/weekly-summary/{summaryId}")
    public ApiResponse<WeeklyReportSummaryResponse> getWeeklySummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 주간 리포트 요약 ID. 생성 응답의 emotionReportId와 동일하게 사용합니다.", example = "12", required = true)
            @PathVariable Long summaryId
    ) {
        return ApiResponse.onSuccess(
                weeklyReportService.getWeeklySummary(userDetails.getMember().getPhoneNumber(), summaryId)
        );
    }
    @Operation(summary = "주간 리포트 조회", description = "지정한 날짜가 포함된 주간 리포트 요약을 조회합니다. 날짜가 월요일이 아니면 해당 주의 월요일로 보정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = WeeklyReportSummaryApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 주간 리포트 없음")
    })
    @GetMapping("/weekly")
    public ApiResponse<WeeklyReportSummaryResponse> getWeeklyByStartDate(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 날짜", example = "2026-07-13", required = true)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            @RequestParam LocalDate startDate
    ) {
        return ApiResponse.onSuccess(weeklyReportService.getWeeklyByStartDate(userDetails.getMember().getPhoneNumber(), startDate));
    }

    @Operation(summary = "주간 리포트 기간 목록 조회", description = "본인에게 생성된 주간 리포트의 기간과 생성 상태를 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = WeeklyReportPeriodsApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패")
    })
    @GetMapping("/weeks")
    public ApiResponse<List<WeeklyReportPeriodResponse>> getWeeklyPeriods(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(weeklyReportService.getWeeklyPeriods(userDetails.getMember().getPhoneNumber()));
    }

    @Operation(summary = "주간 리포트 재생성", description = "기존 주간 리포트의 기간을 기준으로 리포트를 다시 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재생성 시작 성공", content = @Content(schema = @Schema(implementation = GenerateWeeklyReportApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "본인의 주간 리포트 없음")
    })
    @PostMapping("/{reportId}/regenerate")
    public ApiResponse<GenerateWeeklyReportResponse> regenerateWeekly(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "재생성할 주간 리포트 ID", example = "12", required = true)
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(weeklyReportService.regenerate(userDetails.getMember().getPhoneNumber(), reportId));
    }
    @Operation(summary = "주간 리포트 감정 통계 조회")
    @GetMapping("/weekly/{reportId}/emotion-stats")
    public ApiResponse<ReportEmotionStatsResponse> getReportEmotionStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(reportScreenService.emotionStats(userDetails.getMember().getPhoneNumber(), reportId));
    }

    @Operation(summary = "주간 리포트 소각 목록 조회")
    @GetMapping("/weekly/{reportId}/burnings")
    public ApiResponse<List<ReportBurningItemResponse>> getReportBurnings(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(reportScreenService.burnings(userDetails.getMember().getPhoneNumber(), reportId));
    }

    @Operation(summary = "주간 리포트 부적 목록 조회")
    @GetMapping("/weekly/{reportId}/talismans")
    public ApiResponse<ReportTalismansResponse> getReportTalismans(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(reportScreenService.talismans(userDetails.getMember().getPhoneNumber(), reportId));
    }

    @Operation(summary = "주간 리포트 요약 조회")
    @GetMapping("/weekly/{reportId}/summary")
    public ApiResponse<WeeklyReportSummaryResponse> getReportSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(weeklyReportService.getWeeklySummary(userDetails.getMember().getPhoneNumber(), reportId));
    }

    @Operation(summary = "주간 리포트 다음 주 흐름 조회")
    @GetMapping("/weekly/{reportId}/next-week-flow")
    public ApiResponse<NextWeekFlowQueryResponse> getReportNextWeekFlow(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(reportScreenService.nextWeekFlow(userDetails.getMember().getPhoneNumber(), reportId));
    }
    @Operation(summary = "월간 리포트 생성")
    @PostMapping("/monthly/generate")
    public ApiResponse<GenerateWeeklyReportResponse> generateMonthly(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody GenerateMonthlyReportRequest request
    ) {
        return ApiResponse.onSuccess(monthlyReportService.generate(userDetails.getMember().getPhoneNumber(), request));
    }

    @Operation(summary = "월간 리포트 조회")
    @GetMapping("/monthly")
    public ApiResponse<WeeklyReportSummaryResponse> getMonthly(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponse.onSuccess(monthlyReportService.get(userDetails.getMember().getPhoneNumber(), year, month));
    }

    @Operation(summary = "월간 리포트 기간 목록 조회")
    @GetMapping("/months")
    public ApiResponse<List<MonthlyReportPeriodResponse>> getMonthlyPeriods(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(monthlyReportService.getPeriods(userDetails.getMember().getPhoneNumber()));
    }

    @Operation(summary = "월간 리포트 재생성")
    @PostMapping("/monthly/{reportId}/regenerate")
    public ApiResponse<GenerateWeeklyReportResponse> regenerateMonthly(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId
    ) {
        return ApiResponse.onSuccess(monthlyReportService.regenerate(userDetails.getMember().getPhoneNumber(), reportId));
    }}
