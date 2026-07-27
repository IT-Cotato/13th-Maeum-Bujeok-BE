package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCalendarResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryDetailResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Tag(name = "일기", description = "일기 작성, 달력, 상세, 수정, 삭제 및 커서 목록 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(
            summary = "일기 작성",
            description = "기록일 기준 회원당 하루 한 건만 작성합니다. 기록일을 생략하면 Asia/Seoul 기준 오늘을 사용하며 미래 날짜는 허용하지 않습니다. 저장 후 AI 분석을 비동기로 시작합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "작성 성공", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.CreateDiaryApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본문·감정·기록일 또는 이미지 요청 오류", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "연결할 업로드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "해당 기록일에 이미 일기가 존재함", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @PostMapping
    public ApiResponse<CreateDiaryResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateDiaryRequest request
    ) {
        return ApiResponse.onSuccess(diaryService.create(phone(userDetails), request));
    }

    @Operation(
            summary = "내 일기 커서 목록 조회",
            description = "recordedDate와 diaryId 기준 최신순 커서 페이지를 반환합니다. size 기본값은 20, 최댓값은 50입니다. date 또는 year/month를 지정하면 기존 필터 목록 응답을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryCursorPageApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 커서, 크기 또는 날짜 필터", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패")
    })
    @GetMapping
    public ApiResponse<?> getAll(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "특정 기록일 필터(기존 호환용)", example = "2026-07-27") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "연도 필터(기존 호환용, month와 함께 사용)", example = "2026") @RequestParam(required = false) Integer year,
            @Parameter(description = "월 필터(기존 호환용, 1~12)", example = "7") @RequestParam(required = false) Integer month,
            @Parameter(description = "이전 응답의 nextCursor. 첫 페이지에서는 생략", example = "MjAyNi0wNy0yN3w0Mg") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기(기본 20, 최대 50)", example = "20") @RequestParam(required = false) Integer size
    ) {
        if (date != null || year != null || month != null) {
            return ApiResponse.onSuccess(diaryService.getAll(phone(userDetails), date, year, month));
        }
        return ApiResponse.onSuccess(diaryService.getPage(phone(userDetails), cursor, size));
    }

    @Operation(
            summary = "월별 일기 달력 조회",
            description = "일기가 존재하는 날짜만 반환합니다. status는 STORED 또는 BURNED이며, 현재 소각 도메인 연동 전에는 STORED만 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryCalendarApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "연도 또는 월 범위 오류", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패")
    })
    @GetMapping("/calendar")
    public ApiResponse<DiaryCalendarResponse> getCalendar(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 연도", example = "2026", required = true) @RequestParam Integer year,
            @Parameter(description = "조회 월(1~12)", example = "7", required = true) @RequestParam Integer month
    ) {
        return ApiResponse.onSuccess(diaryService.getCalendar(phone(userDetails), year, month));
    }

    @Operation(summary = "일기 상세 조회", description = "일기 원문, 선택 감정, 첨부 이미지의 임시 조회 URL과 AI 분석 결과를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryDetailApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "본인 소유의 일기를 찾을 수 없음", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @GetMapping("/{diaryId}")
    public ApiResponse<DiaryDetailResponse> get(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", example = "42", required = true) @PathVariable Long diaryId
    ) {
        return ApiResponse.onSuccess(diaryService.get(phone(userDetails), diaryId));
    }

    @Operation(
            summary = "일기 수정",
            description = "본문과 선택 감정을 수정합니다. imageUploadIds 생략 시 기존 이미지를 유지하고, 빈 배열이면 모두 제거하며, 값이 있으면 배열 순서대로 전체 교체합니다. 본문 또는 감정이 실제로 변경된 경우에만 AI 분석을 다시 시작합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.UpdateDiaryApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본문·감정 또는 이미지 요청 오류", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일기 또는 연결할 업로드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @PatchMapping("/{diaryId}")
    public ApiResponse<UpdateDiaryResponse> update(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", example = "42", required = true) @PathVariable Long diaryId,
            @RequestBody UpdateDiaryRequest request
    ) {
        return ApiResponse.onSuccess(diaryService.update(phone(userDetails), diaryId, request));
    }

    @Operation(summary = "일기 삭제", description = "일기 원문, AI 분석과 이미지 연결을 물리 삭제합니다. 연결된 객체 파일은 삭제 재시도 대상으로 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "본인 소유의 일기를 찾을 수 없음", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", example = "42", required = true) @PathVariable Long diaryId
    ) {
        diaryService.delete(phone(userDetails), diaryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "대표 감정 통계 조회")
    @GetMapping("/emotion-stats")
    public ApiResponse<List<EmotionStatResponse>> getEmotionStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회 시작일", example = "2026-07-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일", example = "2026-07-27") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.onSuccess(diaryService.getEmotionStats(phone(userDetails), from, to));
    }

    @Operation(summary = "일기 AI 분석 결과 조회")
    @GetMapping("/{diaryId}/analysis")
    public ApiResponse<DiaryAnalysisResponse> getAnalysis(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "일기 ID", example = "42", required = true) @PathVariable Long diaryId
    ) {
        return ApiResponse.onSuccess(diaryService.getAnalysis(phone(userDetails), diaryId));
    }

    private String phone(CustomUserDetails userDetails) {
        return userDetails.getMember().getPhoneNumber();
    }
}
