package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.dto.SajuProfileRequest;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 관련 API", description = "회원 사주 정보 관리 등 사용자 관련 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberSajuProfileRepository sajuProfileRepository;

    @Operation(
            summary = "사주 정보 입력/수정 API",
            description = "로그인된 회원의 추가 정보(성별, 양/음력/윤달, 태어난 시간)를 입력 받아 사주 프로필로 등록합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @PostMapping("/saju")
    public ApiResponse<String> saveSajuProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SajuProfileRequest request
    ) {
        if (userDetails == null) {
            return ApiResponse.onFailure("401", "인증 정보가 올바르지 않습니다.", null);
        }

        Member member = userDetails.getMember();

        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member)
                .map(existing -> MemberSajuProfile.builder()
                        .member(member)
                        .gender(request.getGender())
                        .calendarType(request.getCalendarType())
                        .birthTime(request.getBirthTime())
                        .build())
                .orElseGet(() -> MemberSajuProfile.builder()
                        .member(member)
                        .gender(request.getGender())
                        .calendarType(request.getCalendarType())
                        .birthTime(request.getBirthTime())
                        .build());

        sajuProfileRepository.save(sajuProfile);
        return ApiResponse.onSuccess("사주 정보가 등록되었습니다.");
    }
}
