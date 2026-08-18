package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.dto.TermsDetailResponse;
import com.maumbujeok.backend.domain.member.dto.TermsSummaryResponse;
import com.maumbujeok.backend.domain.member.service.TermsService;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지 이용약관 API", description = "마이페이지 이용약관 목록 및 상세 조회 API")
@RestController
@RequestMapping("/api/mypage/terms")
@RequiredArgsConstructor
public class MypageTermsController {
    private final TermsService termsService;

    @Operation(summary = "이용약관 목록 조회 API", security = @SecurityRequirement(name = "JWT_TOKEN"))
    @GetMapping
    public ApiResponse<List<TermsSummaryResponse>> getTerms() {
        return ApiResponse.onSuccess(termsService.getTerms());
    }

    @Operation(summary = "이용약관 상세 조회 API", security = @SecurityRequirement(name = "JWT_TOKEN"))
    @GetMapping("/{termType}")
    public ApiResponse<TermsDetailResponse> getTermsDetail(@PathVariable String termType) {
        return ApiResponse.onSuccess(termsService.getTermsDetail(termType));
    }
}
