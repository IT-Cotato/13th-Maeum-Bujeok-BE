package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트 API", description = "인증 동작 확인을 위한 테스트 API")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(
            summary = "현재 사용자 정보 조회 API", 
            description = "인증(JWT)이 완료된 상태에서 헤더에 토큰을 실어 호출하면 로그인한 회원의 정보를 리턴합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @GetMapping("/me")
    public ApiResponse<String> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.onFailure("401", "인증 정보가 없습니다.", null);
        }
        return ApiResponse.onSuccess("인증이 완료되었습니다. 현재 로그인 이메일: " + userDetails.getUsername());
    }
}
