package com.maumbujeok.backend.domain.home.controller;

import com.maumbujeok.backend.domain.home.application.HomeSummaryService;
import com.maumbujeok.backend.domain.home.dto.HomeSummaryResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "홈 화면 API", description = "홈 화면 사주 기반 요약(오행 기운, 오늘의 행운, 오행 감정 분석) 관련 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class HomeController {

    private final HomeSummaryService homeSummaryService;

    @Operation(
            summary = "오늘의 홈 화면 요약 조회",
            description = "로그인한 사용자의 사주 데이터(연/월/일/시)를 기반으로 생성된 오늘의 오행 기운, 행운 팁(1문장), 오행 감정 분석(2문장) 요약 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = HomeSwaggerSchemas.HomeSummaryApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "인증 토큰이 없거나 유효하지 않음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "AUTH_001: 사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = HomeSwaggerSchemas.HomeErrorApiResponse.class))
            )
    })
    @GetMapping("/summary")
    public ApiResponse<HomeSummaryResponse> getTodayHomeSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                homeSummaryService.getTodaySummary(userDetails.getMember().getPhoneNumber())
        );
    }
}
