package com.maumbujeok.backend.domain.home.application;

import com.maumbujeok.backend.domain.home.ai.HomeSummaryAiProvider;
import com.maumbujeok.backend.domain.home.ai.HomeSummaryAiResponse;
import com.maumbujeok.backend.domain.home.ai.HomeSummaryComposer;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeSummaryService {

    private final HomeSummaryRepository homeSummaryRepository;
    private final MemberRepository memberRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;
    private final HomeSummaryAiProvider aiProvider;
    private final HomeSummaryComposer fallbackComposer;

    @Transactional(readOnly = true)
    public HomeSummaryResponse getTodaySummary(String phoneNumber) {
        LocalDate today = LocalDate.now();
        return homeSummaryRepository.findByMemberPhoneNumberAndSummaryDate(phoneNumber, today)
                .map(HomeSummaryResponse::from)
                .orElseGet(() -> {
                    Member member = memberRepository.findByPhoneNumber(phoneNumber)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    HomeSummary saved = generateAndSaveForMember(member, today);
                    return HomeSummaryResponse.from(saved);
                });
    }

    @Transactional
    public HomeSummary generateAndSaveForMember(Member member, LocalDate date) {
        if (homeSummaryRepository.existsByMemberPhoneNumberAndSummaryDate(member.getPhoneNumber(), date)) {
            return homeSummaryRepository.findByMemberPhoneNumberAndSummaryDate(member.getPhoneNumber(), date)
                    .orElseThrow();
        }

        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member).orElse(null);

        String gender = sajuProfile != null && sajuProfile.getGender() != null
                ? sajuProfile.getGender().name() : "NONE";
        String calendarType = sajuProfile != null && sajuProfile.getCalendarType() != null
                ? sajuProfile.getCalendarType().name() : "SOLAR";
        String birthDate = member.getBirthDate() != null ? member.getBirthDate() : "19950101";
        String birthTime = sajuProfile != null && sajuProfile.getBirthTime() != null
                ? sajuProfile.getBirthTime().toString() : null;

        HomeSummaryAiResponse aiResponse;
        try {
            aiResponse = aiProvider.generate(
                    member.getName(),
                    gender,
                    calendarType,
                    birthDate,
                    birthTime,
                    date
            );
        } catch (Exception e) {
            log.warn("Home summary AI generation failed for member={}. Using fallback composer. Error: {}",
                    member.getPhoneNumber(), e.getMessage());
            aiResponse = fallbackComposer.compose(
                    member.getName(),
                    gender,
                    calendarType,
                    birthDate,
                    birthTime,
                    date
            );
        }

        HomeSummary homeSummary = HomeSummary.builder()
                .member(member)
                .summaryDate(date)
                .primaryElement(aiResponse.primaryElement())
                .todayLuck(aiResponse.todayLuck())
                .todayEnergy(aiResponse.todayEnergy())
                .modelName("gpt-4o-mini")
                .reportVersion("v1")
                .build();

        return homeSummaryRepository.save(homeSummary);
    }
}
