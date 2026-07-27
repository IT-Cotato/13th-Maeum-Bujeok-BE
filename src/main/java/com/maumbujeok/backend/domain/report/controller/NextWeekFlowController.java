package com.maumbujeok.backend.domain.report.controller;

import com.maumbujeok.backend.domain.report.application.NextWeekFlowService;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowQueryResponse;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowRequest;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowStartResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "다음 주 흐름 API", description = "다음 주 흐름 생성 및 조회 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class NextWeekFlowController {
    private final NextWeekFlowService nextWeekFlowService;

    @Operation(
            summary = "다음 주 흐름 AI 생성 요청",
            description = "사용자의 사주 정보와 해당 주차 감정 흐름 데이터를 바탕으로 다음 주 흐름 분석을 시작합니다."
    )
    @PostMapping("/next-week-flow")
    public ApiResponse<NextWeekFlowStartResponse> generateNextWeekFlow(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NextWeekFlowRequest request
    ) {
        return ApiResponse.onSuccess(
                nextWeekFlowService.generate(userDetails.getMember().getPhoneNumber(), request)
        );
    }

    @Operation(
            summary = "다음 주 흐름 결과 조회",
            description = "생성 요청한 다음 주 흐름 분석 상태와 조언 텍스트를 조회합니다."
    )
    @GetMapping("/next-week-flow/{flowId}")
    public ApiResponse<NextWeekFlowQueryResponse> getNextWeekFlow(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long flowId
    ) {
        return ApiResponse.onSuccess(
                nextWeekFlowService.getFlow(userDetails.getMember().getPhoneNumber(), flowId)
        );
    }
}
