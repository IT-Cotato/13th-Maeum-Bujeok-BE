package com.maumbujeok.backend.domain.diary.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DiaryServiceEventTest {
    @Mock MemberRepository memberRepository;
    @Mock DiaryRepository diaryRepository;
    @Mock DiaryAnalysisRepository analysisRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks DiaryService diaryService;

    @Captor ArgumentCaptor<Object> eventCaptor;

    @Test
    void publishesWeeklyReportRefreshEventWhenDiaryIsCreated() {
        Member member = Member.builder()
                .name("테스터")
                .phoneNumber("01011112222")
                .role(Member.Role.ROLE_USER)
                .build();
        Diary savedDiary = new Diary(member, "오늘은 괜찮았다", DiaryEmotion.HAPPY);
        DiaryAnalysis savedAnalysis = new DiaryAnalysis(savedDiary, DiaryService.PROMPT_VERSION, DiaryService.POLICY_VERSION);
        when(memberRepository.getReferenceById(member.getPhoneNumber())).thenReturn(member);
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);
        when(analysisRepository.save(any(DiaryAnalysis.class))).thenReturn(savedAnalysis);

        diaryService.create(member.getPhoneNumber(), new CreateDiaryRequest("오늘은 괜찮았다", "HAPPY"));

        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        assertInstanceOf(DiaryCreatedEvent.class, eventCaptor.getAllValues().get(0));
        DiaryWeeklyReportRefreshRequestedEvent refreshEvent = assertInstanceOf(
                DiaryWeeklyReportRefreshRequestedEvent.class,
                eventCaptor.getAllValues().get(1)
        );
        assertEquals(member.getPhoneNumber(), refreshEvent.memberPhoneNumber());
        assertEquals(LocalDate.now(), refreshEvent.diaryDate());
    }
}
