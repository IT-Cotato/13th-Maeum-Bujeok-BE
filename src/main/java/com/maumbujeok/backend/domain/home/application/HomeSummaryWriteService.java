package com.maumbujeok.backend.domain.home.application;

import com.maumbujeok.backend.domain.home.ai.HomeSummaryAiResponse;
import com.maumbujeok.backend.domain.home.domain.HomeSummary;
import com.maumbujeok.backend.domain.home.repository.HomeSummaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeSummaryWriteService {

    private final HomeSummaryRepository homeSummaryRepository;
    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public HomeSummary saveOrUpdate(String phoneNumber, LocalDate date, HomeSummaryAiResponse aiResponse) {
        Member member = memberRepository.findByPhoneNumberForUpdate(phoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        HomeSummary homeSummary = homeSummaryRepository.findByMemberPhoneNumberAndSummaryDate(phoneNumber, date)
                .orElse(null);

        if (homeSummary != null) {
            homeSummary.update(
                    aiResponse.primaryElement(),
                    aiResponse.todayLuck(),
                    aiResponse.todayEnergy(),
                    "gpt-4o-mini",
                    "v1"
            );
            return homeSummary;
        }

        HomeSummary newSummary = HomeSummary.builder()
                .member(member)
                .summaryDate(date)
                .primaryElement(aiResponse.primaryElement())
                .todayLuck(aiResponse.todayLuck())
                .todayEnergy(aiResponse.todayEnergy())
                .modelName("gpt-4o-mini")
                .reportVersion("v1")
                .build();

        return homeSummaryRepository.saveAndFlush(newSummary);
    }
}
