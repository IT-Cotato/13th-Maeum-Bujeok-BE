package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.dto.MemberProfileResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class MemberSwaggerSchemas {
    private MemberSwaggerSchemas() {
    }

    @Schema(name = "MemberProfileApiResponse", description = "내 회원 정보 조회 성공 응답")
    static final class MemberProfileApiResponse extends ApiResponse<MemberProfileResponse> {
        private MemberProfileApiResponse() {
            super(true, "200", "요청이 성공했습니다.", null);
        }
    }

    @Schema(name = "MemberOnboardingApiResponse", description = "온보딩 저장 성공 응답")
    static final class MemberOnboardingApiResponse extends ApiResponse<String> {
        private MemberOnboardingApiResponse() {
            super(true, "200", "온보딩 정보가 등록되었습니다.", "온보딩 정보가 등록되었습니다.");
        }
    }
}