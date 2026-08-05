package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.dto.OnboardingRequest;
import com.maumbujeok.backend.domain.member.dto.MemberProfileResponse;
import com.maumbujeok.backend.domain.member.repository.MemberNotificationSettingRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "회원 관련 API", description = "회원 사주 정보 관리 및 회원 탈퇴 등 사용자 관련 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;
    private final MemberNotificationSettingRepository notificationSettingRepository;
    private final TalismanRepository talismanRepository;
    private final NextWeekFlowRepository nextWeekFlowRepository;
    private final EmotionReportRepository emotionReportRepository;
    private final DiaryAnalysisRepository diaryAnalysisRepository;
    private final DiaryRepository diaryRepository;

    @Operation(
            summary = "내 회원 정보 조회 API",
            description = "로그인한 사용자의 계정, 온보딩, 사주 및 동의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberProfileApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "인증 토큰이 없거나 유효하지 않은 요청. Spring Security 기본 오류 응답",
                    content = @Content(
                            schema = @Schema(implementation = MemberSwaggerSchemas.MemberForbiddenErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"timestamp\":\"2026-08-05T11:45:37.735+09:00\",\"status\":403,\"error\":\"Forbidden\",\"path\":\"/api/members/me\"}"
                            )
                    )
            )
    })    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ApiResponse.onFailure("401", "인증 정보가 올바르지 않습니다.", null);
        }

        Member member = userDetails.getMember();
        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member).orElse(null);
        return ApiResponse.onSuccess(MemberProfileResponse.from(member, sajuProfile));
    }
    @Operation(
            summary = "온보딩 완료 API",
            description = "로그인된 회원의 생년월일, 사주 정보와 약관 동의 내역을 저장합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "온보딩 정보 저장 성공",
                    content = @Content(schema = @Schema(implementation = MemberSwaggerSchemas.MemberOnboardingApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "COMMON_400: 필수값, 약관 동의 또는 생년월일 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = MemberSwaggerSchemas.OnboardingValidationErrorApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 생년월일",
                                            value = "{\"success\":false,\"code\":\"COMMON_400\",\"message\":\"생년월일은 yyyyMMdd 형식의 실제 날짜여야 합니다.\",\"data\":null}"
                                    ),
                                    @ExampleObject(
                                            name = "필수 약관 미동의",
                                            value = "{\"success\":false,\"code\":\"COMMON_400\",\"message\":\"필수 서비스 이용약관에 동의해야 합니다.\",\"data\":null}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "인증 토큰이 없거나 유효하지 않은 요청. Spring Security 기본 오류 응답",
                    content = @Content(
                            schema = @Schema(implementation = MemberSwaggerSchemas.MemberForbiddenErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"timestamp\":\"2026-08-05T11:45:37.735+09:00\",\"status\":403,\"error\":\"Forbidden\",\"path\":\"/api/members/onboarding\"}"
                            )
                    )
            )
    })    @PostMapping("/onboarding")
    @Transactional
    public ApiResponse<String> completeOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @jakarta.validation.Valid @RequestBody OnboardingRequest request
    ) {
        if (userDetails == null) {
            return ApiResponse.onFailure("401", "인증 정보가 올바르지 않습니다.", null);
        }

        Member member = userDetails.getMember();
        LocalDateTime now = LocalDateTime.now();
        member.completeOnboarding(
                request.getBirthDate(), now, now,
                Boolean.TRUE.equals(request.getSensitiveDataAgreed()) ? now : null,
                Boolean.TRUE.equals(request.getMarketingAgreed()) ? now : null
        );

        Member savedMember = memberRepository.save(member);

        sajuProfileRepository.findByMember(savedMember)
                .ifPresentOrElse(
                        existing -> existing.update(request.getGender(), request.getCalendarType(), request.getBirthTime()),
                        () -> sajuProfileRepository.save(MemberSajuProfile.builder()
                                .member(savedMember)
                                .gender(request.getGender())
                                .calendarType(request.getCalendarType())
                                .birthTime(request.getBirthTime())
                                .build())
                );

        return ApiResponse.onSuccess("온보딩 정보가 등록되었습니다.");
    }
    @Operation(
            summary = "회원 탈퇴 API",
            description = "현재 로그인된 사용자의 모든 관련 데이터(부적, 다음주흐름, 감정리포트, 일기, 알림설정, 사주프로필, Refresh Token) 및 회원 본인 정보를 DB에서 완전히 삭제(Hard Delete)합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @Transactional
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 및 소유 데이터 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "인증 토큰이 없거나 유효하지 않은 요청. Spring Security 기본 오류 응답",
                    content = @Content(
                            schema = @Schema(implementation = MemberSwaggerSchemas.MemberForbiddenErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = "{\"timestamp\":\"2026-08-05T11:45:37.735+09:00\",\"status\":403,\"error\":\"Forbidden\",\"path\":\"/api/members/withdraw\"}"
                            )
                    )
            )
    })    @DeleteMapping("/withdraw")
    public ApiResponse<String> withdrawMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.onFailure("401", "인증 정보가 올바르지 않습니다.", null);
        }

        Member member = userDetails.getMember();
        String phoneNumber = member.getPhoneNumber();

        // 1. Refresh Token 삭제
        String userKey = phoneNumber != null ? phoneNumber : "GOOGLE_" + member.getProviderId();
        refreshTokenRepository.deleteByUserKey(userKey);

        if (phoneNumber != null) {
            // 2. 부적 리스트 삭제
            talismanRepository.deleteByMemberPhoneNumber(phoneNumber);

            // 3. 다음 주 흐름 삭제
            nextWeekFlowRepository.deleteByMemberPhoneNumber(phoneNumber);

            // 4. 감정 리포트 삭제
            emotionReportRepository.deleteByMemberPhoneNumber(phoneNumber);

            // 5. 일기 분석 및 일기 삭제
            java.util.List<com.maumbujeok.backend.domain.diary.domain.Diary> diaries =
                    diaryRepository.findAllByMemberPhoneNumberOrderByRecordedDateDescCreatedAtDescIdDesc(phoneNumber);
            if (!diaries.isEmpty()) {
                diaryAnalysisRepository.deleteByDiaryIn(diaries);
                diaryRepository.deleteAllInBatch(diaries);
            }

            // 6. 알림 설정 삭제
            notificationSettingRepository.deleteByMemberPhoneNumber(phoneNumber);

            // 7. 사주 프로필 삭제
            sajuProfileRepository.findByMember(member).ifPresent(sajuProfileRepository::delete);
        }

        // 8. Member 삭제
        memberRepository.delete(member);

        return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
    }
}
