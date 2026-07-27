package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.dto.SajuProfileRequest;
import com.maumbujeok.backend.domain.member.repository.MemberNotificationSettingRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    @Operation(
            summary = "회원 탈퇴 API",
            description = "현재 로그인된 사용자의 모든 관련 데이터(부적, 다음주흐름, 감정리포트, 일기, 알림설정, 사주프로필, Refresh Token) 및 회원 본인 정보를 DB에서 완전히 삭제(Hard Delete)합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @Transactional
    @DeleteMapping("/withdraw")
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
                    diaryRepository.findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(phoneNumber);
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
