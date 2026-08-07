package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.dto.MemberProfileResponse;
import com.maumbujeok.backend.domain.member.dto.MemberProfileUpdateResponse;
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

    @Schema(name = "MemberProfileUpdateApiResponse", description = "내 회원 정보 수정 성공 응답")
    static final class MemberProfileUpdateApiResponse extends ApiResponse<MemberProfileUpdateResponse> {
        private MemberProfileUpdateApiResponse() {
            super(true, "200", "요청이 성공했습니다.", null);
        }
    }

    @Schema(name = "MemberOnboardingApiResponse", description = "온보딩 저장 성공 응답")
    static final class MemberOnboardingApiResponse extends ApiResponse<String> {
        private MemberOnboardingApiResponse() {
            super(true, "200", "온보딩 정보가 등록되었습니다.", "온보딩 정보가 등록되었습니다.");
        }
    }

    @Schema(name = "OnboardingValidationErrorApiResponse", description = "온보딩 검증 실패 공통 응답")
    static final class OnboardingValidationErrorApiResponse extends ApiResponse<Void> {
        private OnboardingValidationErrorApiResponse() {
            super(false, "COMMON_400", "생년월일은 yyyyMMdd 형식의 실제 날짜여야 합니다.", null);
        }
    }

    @Schema(name = "MemberValidationErrorApiResponse", description = "회원 프로필 수정 검증 실패 응답")
    static final class MemberValidationErrorApiResponse extends ApiResponse<Void> {
        private MemberValidationErrorApiResponse() {
            super(false, "COMMON_400", "전화번호는 하이픈 포함 또는 제외한 10~11자리 숫자여야 합니다.", null);
        }
    }

    @Schema(name = "MemberSajuProfileNotFoundApiResponse", description = "회원 사주 프로필 미존재 응답")
    static final class MemberSajuProfileNotFoundApiResponse extends ApiResponse<Void> {
        private MemberSajuProfileNotFoundApiResponse() {
            super(false, "MEMBER_003", "사주 프로필 정보를 찾을 수 없습니다. 먼저 온보딩을 완료해주세요.", null);
        }
    }

    @Schema(name = "MemberPhoneConflictApiResponse", description = "회원 전화번호 중복 응답")
    static final class MemberPhoneConflictApiResponse extends ApiResponse<Void> {
        private MemberPhoneConflictApiResponse() {
            super(false, "AUTH_009", "이미 가입된 전화번호입니다.", null);
        }
    }

    @Schema(name = "MemberForbiddenErrorResponse", description = "인증되지 않은 회원 API 요청의 Spring Security 기본 오류 응답")
    static final class MemberForbiddenErrorResponse {
        @Schema(example = "2026-08-05T11:45:37.735+09:00")
        public String timestamp;

        @Schema(example = "403")
        public int status;

        @Schema(example = "Forbidden")
        public String error;

        @Schema(example = "/api/members/me")
        public String path;
    }
}
