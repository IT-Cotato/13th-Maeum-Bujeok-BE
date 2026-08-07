package com.maumbujeok.backend.domain.saju.controller;

import com.maumbujeok.backend.domain.saju.application.SajuAnalysisService;
import com.maumbujeok.backend.domain.saju.dto.CreateSajuAnalysisResponse;
import com.maumbujeok.backend.domain.saju.dto.SajuAnalysisResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/saju/analyses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "사주 분석 API", description = "사주 오행 비율 분석 요청 및 상태 조회 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class SajuAnalysisController {
    private final SajuAnalysisService sajuAnalysisService;

    @Operation(
            summary = "사주 분석 요청 생성",
            description = "현재 로그인한 사용자의 저장된 사주 프로필을 기준으로 사주 오행 비율 분석 요청을 생성합니다. 생성 응답은 PENDING이지만 트랜잭션 커밋 직후 비동기 AI 분석이 자동으로 시작됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.CreateSajuAnalysisApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 불가 (SAJU_400)",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.SajuAnalysisErrorApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사주 프로필 없음 (MEMBER_003)",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.SajuAnalysisErrorApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패")
    })
    @PostMapping
    public ApiResponse<CreateSajuAnalysisResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                sajuAnalysisService.create(userDetails.getMember().getPhoneNumber())
        );
    }

    @Operation(
            summary = "사주 분석 상태 조회",
            description = "사주 분석 요청의 상태와 결과를 조회합니다. 조회 API는 상태를 변경하지 않으며, 생성 또는 회원 정보 저장 시점에 시작된 비동기 AI 분석 결과를 그대로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.SajuAnalysisApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "본인 소유 분석 요청 없음 (SAJU_404)",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.SajuAnalysisErrorApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패")
    })
    @GetMapping("/{analysisId}")
    public ApiResponse<SajuAnalysisResponse> get(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 사주 분석 ID", example = "1", required = true)
            @PathVariable Long analysisId
    ) {
        return ApiResponse.onSuccess(
                sajuAnalysisService.get(userDetails.getMember().getPhoneNumber(), analysisId)
        );
    }

    @Operation(
            summary = "내 최신 사주 분석 조회",
            description = "현재 로그인한 사용자의 최신 사주 분석 상태와 결과를 조회합니다. 온보딩 또는 프로필 수정으로 자동 재분석된 결과를 확인할 때 사용할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.SajuAnalysisApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "최신 사주 분석 요청 없음 (SAJU_404)",
                    content = @Content(schema = @Schema(implementation = SajuSwaggerSchemas.SajuAnalysisErrorApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "JWT 인증 실패")
    })
    @GetMapping("/latest")
    public ApiResponse<SajuAnalysisResponse> getLatest(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                sajuAnalysisService.getLatest(userDetails.getMember().getPhoneNumber())
        );
    }
}
