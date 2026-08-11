package com.maumbujeok.backend.domain.saju.application;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.saju.ai.SajuAiPromptVersion;
import com.maumbujeok.backend.domain.saju.ai.SajuAiResult;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class SajuAnalysisServiceTest {

    @Mock SajuAnalysisRepository sajuAnalysisRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    private SajuAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new SajuAnalysisService(null, null, sajuAnalysisRepository, eventPublisher);
    }

    @Test
    void reusesExistingAnalysisWhenUniqueConstraintConflictOccurs() {
        Member member = Member.builder()
                .phoneNumber("01040000001")
                .name("테스트")
                .birthDate("19900101")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build();
        MemberSajuProfile sajuProfile = MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.FEMALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(LocalTime.of(7, 30))
                .build();
        SajuAnalysis existing = completedAnalysis(member, sajuProfile);

        when(sajuAnalysisRepository.findAllByMemberPhoneNumberForUpdate(member.getPhoneNumber()))
                .thenReturn(List.of(), List.of(existing));
        when(sajuAnalysisRepository.saveAndFlush(any(SajuAnalysis.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        SajuAnalysis result = service.refreshLatestAnalysis(member, sajuProfile, false);

        assertSame(existing, result);
        verify(sajuAnalysisRepository, times(2)).findAllByMemberPhoneNumberForUpdate(member.getPhoneNumber());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private SajuAnalysis completedAnalysis(Member member, MemberSajuProfile sajuProfile) {
        SajuAnalysis analysis = new SajuAnalysis(
                member,
                member.getBirthDate(),
                sajuProfile.getGender(),
                sajuProfile.getCalendarType(),
                sajuProfile.getBirthTime(),
                SajuAiPromptVersion.VALUE
        );
        analysis.markProcessing(1L);
        analysis.complete(1L, new SajuAiResult(20, 20, 20, 20, 20, "fake-saju-v1"), 1);
        return analysis;
    }
}
