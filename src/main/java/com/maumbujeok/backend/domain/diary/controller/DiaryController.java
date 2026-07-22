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
import com.maumbujeok.backend.domain.diary.controller.DiarySwaggerSchemas.DiaryListApiResponse;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
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
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Tag(name = "일기 API", description = "감정 일기 작성, 목록 조회, AI 분석 결과 조회 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(
            summary = "일기 작성",
            description = "감정과 일기 내용을 저장하고 AI 분석을 비동기로 시작합니다. 응답의 diaryId로 분석 조회 API를 호출해 상태를 확인할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "작성 성공 (분석 상태는 PENDING)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateDiaryApiResponse.class),
                            examples = @ExampleObject(value = CREATE_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 오류 (DIARY_400)",
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
                    description = "서버 오류 (COMMON_500)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = INTERNAL_SERVER_ERROR))
            )
    })
    @PostMapping
    public ApiResponse<CreateDiaryResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "감정 코드와 1~5000자의 일기 내용"
            )
            @RequestBody CreateDiaryRequest request
    ) {
        return ApiResponse.onSuccess(diaryService.create(userDetails.getMember().getPhoneNumber(), request));
    }

    @Operation(
            summary = "내 일기 목록 조회",
            description = "로그인한 사용자의 일기를 최신 작성순으로 조회합니다. 작성한 일기가 없으면 data는 빈 배열입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DiaryListApiResponse.class),
                            examples = @ExampleObject(value = LIST_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (COMMON_500)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = INTERNAL_SERVER_ERROR))
            )
    })
    @GetMapping
    public ApiResponse<List<DiaryResponse>> getAll(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(diaryService.getAll(userDetails.getMember().getPhoneNumber()));
    }

    @Operation(
            summary = "일기 AI 분석 결과 조회",
            description = "일기 작성 후 AI 분석 상태와 결과를 조회합니다. PENDING 또는 PROCESSING이면 잠시 후 다시 호출하고, COMPLETED 또는 FALLBACK_COMPLETED이면 결과를 표시합니다. 본인 소유가 아닌 일기도 DIARY_404로 응답합니다."
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
                    description = "일기 없음 또는 접근 권한 없음 (DIARY_404)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = DIARY_NOT_FOUND))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (COMMON_500)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = INTERNAL_SERVER_ERROR))
            )
    })
    @GetMapping("/{diaryId}/analysis")
    public ApiResponse<DiaryAnalysisResponse> getAnalysis(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 일기 ID", example = "42", required = true)
            @PathVariable Long diaryId
    ) {
        return ApiResponse.onSuccess(diaryService.getAnalysis(userDetails.getMember().getPhoneNumber(), diaryId));
    }
}
