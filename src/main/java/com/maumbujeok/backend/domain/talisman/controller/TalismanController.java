package com.maumbujeok.backend.domain.talisman.controller;

import com.maumbujeok.backend.domain.talisman.dto.TalismanListResponse;
import com.maumbujeok.backend.domain.talisman.service.TalismanService;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/talismans")
@RequiredArgsConstructor
@Tag(name = "부적 API", description = "사용자의 부적 관련 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class TalismanController {
    private final TalismanService talismanService;

    @Operation(
            summary = "나의 부적 리스트 조회",
            description = "로그인한 사용자의 부적 목록을 최신순으로 조회합니다."
    )
    @GetMapping
    public ApiResponse<TalismanListResponse> getMyTalismans(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                talismanService.getTalismans(userDetails.getMember().getPhoneNumber())
        );
    }
}
