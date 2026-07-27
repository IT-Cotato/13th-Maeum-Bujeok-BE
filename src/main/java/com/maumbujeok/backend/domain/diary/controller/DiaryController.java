package com.maumbujeok.backend.domain.diary.controller;

import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.ANALYSIS_COMPLETED;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.ANALYSIS_PENDING;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.CREATE_SUCCESS;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.DIARY_NOT_FOUND;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.INTERNAL_SERVER_ERROR;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.INVALID_CONTENT;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.INVALID_EMOTION;
import static com.maumbujeok.backend.domain.diary.controller.DiarySwaggerExamples.LIST_SUCCESS;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.CreateDiaryApiResponse;
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.DiaryAnalysisApiResponse;
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.DiaryDetailApiResponse;
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.DiaryEmotionStatsApiResponse;
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.DiaryListApiResponse;
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.UpdateDiaryApiResponse;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
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
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "일기 API", description = "감정 일기 작성, 조회, 수정, AI 분석 결과 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(summary = "일기 작성", description = "원문을 먼저 저장하고 AI 분석을 비동기로 시작합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "작성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateDiaryApiResponse.class),
                            examples = @ExampleObject(value = CREATE_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "일기 내용 오류", value = INVALID_CONTENT),
                                    @ExampleObject(name = "감정 코드 오류", value = INVALID_EMOTION)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = INTERNAL_SERVER_ERROR)
                    )
            )
    })
    @PostMapping
    public ApiResponse<CreateDiaryResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateDiaryRequest request
    ) {
        return ApiResponse.onSuccess(
                diaryService.create(userDetails.getMember().getPhoneNumber(), request)
        );
    }

    @Operation(
            summary = "내 일기 목록 조회",
            description = "date 또는 year/month로 필터링할 수 있습니다. 필터가 없으면 전체 목록을 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DiaryListApiResponse.class),
                    examples = @ExampleObject(value = LIST_SUCCESS)
            )
    )
    @GetMapping
    public ApiResponse<List<DiaryResponse>> getAll(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                diaryService.getAll(
                        userDetails.getMember().getPhoneNumber(),
                        date,
                        year,
                        month
                )
        );
    }

    @Operation(summary = "일기 단건 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = DiaryDetailApiResponse.class))
    )
    @GetMapping("/{diaryId}")
    public ApiResponse<DiaryResponse> get(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long diaryId
    ) {
        return ApiResponse.onSuccess(
                diaryService.get(userDetails.getMember().getPhoneNumber(), diaryId)
        );
    }

    @Operation(
            summary = "일기 수정",
            description = "본문 또는 선택 감정을 수정하며 기록일은 유지합니다. 실제 변경 시 AI 분석을 다시 시작합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = UpdateDiaryApiResponse.class))
    )
    @PatchMapping("/{diaryId}")
    public ApiResponse<UpdateDiaryResponse> update(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long diaryId,
            @RequestBody UpdateDiaryRequest request
    ) {
        return ApiResponse.onSuccess(
                diaryService.update(
                        userDetails.getMember().getPhoneNumber(),
                        diaryId,
                        request
                )
        );
    }

    @Operation(
            summary = "대표 감정 통계 조회",
            description = "AI 분석이 완료되거나 fallback 완료된 일반 일기만 9가지 대표 감정으로 집계합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = DiaryEmotionStatsApiResponse.class))
    )
    @GetMapping("/emotion-stats")
    public ApiResponse<List<EmotionStatResponse>> getEmotionStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.onSuccess(
                diaryService.getEmotionStats(
                        userDetails.getMember().getPhoneNumber(),
                        from,
                        to
                )
        );
    }

    @Operation(
            summary = "일기 AI 분석 결과 조회",
            description = "본인 소유가 아닌 일기도 DIARY_404로 응답합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DiaryAnalysisApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "분석 완료", value = ANALYSIS_COMPLETED),
                                    @ExampleObject(name = "분석 대기", value = ANALYSIS_PENDING)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "일기 없음 또는 접근 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = DIARY_NOT_FOUND)
                    )
            )
    })
    @GetMapping("/{diaryId}/analysis")
    public ApiResponse<DiaryAnalysisResponse> getAnalysis(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long diaryId
    ) {
        return ApiResponse.onSuccess(
                diaryService.getAnalysis(
                        userDetails.getMember().getPhoneNumber(),
                        diaryId
                )
        );
    }
}
