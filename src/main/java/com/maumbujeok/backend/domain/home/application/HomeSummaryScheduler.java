package com.maumbujeok.backend.domain.home.application;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeSummaryScheduler {

    private final MemberRepository memberRepository;
    private final HomeSummaryService homeSummaryService;

    /**
     * 매일 자정(00:00:00)에 온보딩을 완료한 회원의 홈 화면 요약을 사전 생성하는 스케줄러 배치
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateMidnightHomeSummaries() {
        LocalDate today = LocalDate.now();
        List<Member> onboardedMembers = memberRepository.findAllByOnboardingCompletedAtIsNotNull();
        log.info("Starting midnight home summary generation for {} onboarded members (date: {})",
                onboardedMembers.size(), today);

        int count = 0;
        for (Member member : onboardedMembers) {
            try {
                homeSummaryService.generateAndSaveForMember(member, today);
                count++;
            } catch (Exception e) {
                log.error("Failed to generate midnight home summary for member: {}", member.getPhoneNumber(), e);
            }
        }
        log.info("Completed midnight home summary generation. Successfully processed {}/{} members.",
                count, onboardedMembers.size());
    }
}
