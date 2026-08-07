package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.dto.MemberProfileResponse;
import com.maumbujeok.backend.domain.member.dto.MemberProfileUpdateRequest;
import com.maumbujeok.backend.domain.member.dto.MemberProfileUpdateResponse;
import com.maumbujeok.backend.domain.member.service.MemberProfileService;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지 프로필 API", description = "마이페이지 프로필 조회 및 수정 API")
@RestController
@RequestMapping("/api/mypage/profile")
@RequiredArgsConstructor
public class MypageProfileController {

    private final MemberProfileService memberProfileService;

    @Operation(
            summary = "내 프로필 조회 API",
            description = "로그인된 사용자의 계정, 온보딩, 사주 및 동의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberProfileApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "인증 토큰이 없거나 유효하지 않은 요청. Spring Security 기본 오류 응답",
                    content = @Content(
                            schema = @Schema(implementation = MemberSwaggerSchemas.MemberForbiddenErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"timestamp\":\"2026-08-05T11:45:37.735+09:00\",\"status\":403,\"error\":\"Forbidden\",\"path\":\"/api/mypage/profile\"}"
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<MemberProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                memberProfileService.getMyProfile(userDetails.getMember().getPhoneNumber())
        );
    }

    @Operation(
            summary = "내 프로필 수정 API",
            description = "로그인한 사용자의 이름, 생년월일, 태어난 시간, 전화번호, 성별을 수정합니다. 전화번호 변경 시에는 새 번호에 대한 SMS 인증 완료가 필요합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberProfileUpdateApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "COMMON_400 또는 SMS_003: 요청 값 검증 실패 또는 새 전화번호 SMS 인증 미완료",
                    content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberValidationErrorApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "AUTH_001 또는 MEMBER_003: 회원 또는 사주 프로필 정보 없음",
                    content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberSajuProfileNotFoundApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "AUTH_009: 이미 사용 중인 전화번호",
                    content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberPhoneConflictApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "인증 토큰이 없거나 유효하지 않은 요청. Spring Security 기본 오류 응답",
                    content = @Content(
                            schema = @Schema(implementation = MemberSwaggerSchemas.MemberForbiddenErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"timestamp\":\"2026-08-05T11:45:37.735+09:00\",\"status\":403,\"error\":\"Forbidden\",\"path\":\"/api/mypage/profile\"}"
                            )
                    )
            )
    })
    @PatchMapping
    public ApiResponse<MemberProfileUpdateResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @jakarta.validation.Valid @RequestBody MemberProfileUpdateRequest request
    ) {
        return ApiResponse.onSuccess(
                memberProfileService.updateMyProfile(userDetails.getMember().getPhoneNumber(), request)
        );
    }
}
