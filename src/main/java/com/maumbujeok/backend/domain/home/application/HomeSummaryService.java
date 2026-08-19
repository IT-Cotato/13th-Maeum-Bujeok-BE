package com.maumbujeok.backend.domain.home.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.home.ai.HomeSummaryAiProvider;
import com.maumbujeok.backend.domain.home.ai.HomeSummaryAiResponse;
import com.maumbujeok.backend.domain.home.ai.HomeSummaryComposer;
import com.maumbujeok.backend.domain.home.ai.TodayDiaryInput;
import com.maumbujeok.backend.domain.home.domain.HomeSummary;
import com.maumbujeok.backend.domain.home.dto.HomeSummaryResponse;
import com.maumbujeok.backend.domain.home.repository.HomeSummaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeSummaryService {

    private final HomeSummaryRepository homeSummaryRepository;
    private final MemberRepository memberRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;
    private final DiaryRepository diaryRepository;
    private final HomeSummaryAiProvider aiProvider;
    private final HomeSummaryComposer fallbackComposer;
    private final HomeSummaryWriteService homeSummaryWriteService;
    private final Clock serviceClock;

    public HomeSummaryResponse getTodaySummary(String phoneNumber) {
        LocalDate today = LocalDate.now(serviceClock);
        return homeSummaryRepository.findByMemberPhoneNumberAndSummaryDate(phoneNumber, today)
                .map(HomeSummaryResponse::from)
                .orElseGet(() -> {
                    HomeSummary saved = refreshForToday(phoneNumber);
                    return HomeSummaryResponse.from(saved);
                });
    }

    public HomeSummary refreshForToday(String phoneNumber) {
        LocalDate today = LocalDate.now(serviceClock);
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_SAJU_PROFILE_NOT_FOUND));

        String gender = sajuProfile.getGender() != null ? sajuProfile.getGender().name() : "NONE";
        String calendarType = sajuProfile.getCalendarType() != null ? sajuProfile.getCalendarType().name() : "SOLAR";
        String birthDate = member.getBirthDate() != null ? member.getBirthDate() : "19950101";
        String birthTime = sajuProfile.getBirthTime() != null ? sajuProfile.getBirthTime().toString() : null;

        List<Diary> activeDiaries = diaryRepository
                .findAllByMemberPhoneNumberAndRecordedDateAndBurnedAtIsNullOrderByIdDesc(phoneNumber, today);

        List<TodayDiaryInput> diaryInputs = activeDiaries.stream()
                .limit(5)
                .sorted(java.util.Comparator.comparing(Diary::getId))
                .map(d -> new TodayDiaryInput(
                        d.getSelectedEmotion() != null ? d.getSelectedEmotion().name() : "NONE",
                        truncate(d.getContent(), 200)
                ))
                .toList();

        HomeSummaryAiResponse aiResponse;
        try {
            aiResponse = aiProvider.generate(
                    member.getName(),
                    gender,
                    calendarType,
                    birthDate,
                    birthTime,
                    today,
                    diaryInputs
            );
        } catch (Exception e) {
            log.warn("Home summary AI generation failed for member={}. Using fallback composer. Error: {}",
                    phoneNumber, e.getMessage());
            aiResponse = fallbackComposer.compose(
                    member.getName(),
                    gender,
                    calendarType,
                    birthDate,
                    birthTime,
                    today,
                    diaryInputs
            );
        }

        return homeSummaryWriteService.saveOrUpdate(phoneNumber, today, aiResponse);
    }

    public HomeSummary generateAndSaveForMember(Member member, LocalDate date) {
        return refreshForToday(member.getPhoneNumber());
    }

    private String truncate(String content, int maxLen) {
        if (content == null) return "";
        String trimmed = content.trim();
        return trimmed.length() <= maxLen ? trimmed : trimmed.substring(0, maxLen);
    }
}
