package com.maumbujeok.backend.domain.home.application;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeSummaryScheduler {

    private final MemberRepository memberRepository;
    private final HomeSummaryService homeSummaryService;
    private final java.time.Clock serviceClock;

    /**
     * 매일 자정(00:00:00)에 온보딩을 완료한 회원의 홈 화면 요약을 사전 생성하는 스케줄러 배치 (페이지 단위 순회)
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void generateMidnightHomeSummaries() {
        LocalDate today = LocalDate.now(serviceClock);
        log.info("Starting midnight home summary generation (date: {})", today);

        int pageSize = 100;
        int pageNumber = 0;
        int totalProcessed = 0;
        Page<Member> page;

        do {
            page = memberRepository.findAllByOnboardingCompletedAtIsNotNull(PageRequest.of(pageNumber, pageSize));
            for (Member member : page.getContent()) {
                try {
                    homeSummaryService.generateAndSaveForMember(member, today);
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("Failed to generate midnight home summary for member: {}", member.getPhoneNumber(), e);
                }
            }
            pageNumber++;
        } while (page.hasNext());

        log.info("Completed midnight home summary generation. Successfully processed {} members across {} pages.",
                totalProcessed, pageNumber);
    }
}
