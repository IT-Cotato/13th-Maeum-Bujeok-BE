package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.BurningAiResult;
import com.maumbujeok.backend.domain.burn.domain.*;
import com.maumbujeok.backend.domain.burn.dto.*;
import com.maumbujeok.backend.domain.burn.repository.*;
import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BurningServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock DiaryRepository diaryRepository;
    @Mock BurningRepository burningRepository;
    @Mock BurningAnalysisRepository analysisRepository;
    @Mock TalismanRepository talismanRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks BurningService service;

    @Test
    void createsDirectBurningAndPublishesAnalysisEvent() {
        Member member = Member.builder().phoneNumber("01000000000").build();
        Burning burning = new Burning(member, BurningSourceType.DIRECT, null, "remember", LocalDateTime.now());
        BurningAnalysis analysis = new BurningAnalysis(burning);
        when(memberRepository.getReferenceById(member.getPhoneNumber())).thenReturn(member);
        when(burningRepository.saveAndFlush(any(Burning.class))).thenReturn(burning);
        when(analysisRepository.save(any(BurningAnalysis.class))).thenReturn(analysis);

        CreateBurningResponse response = service.create(member.getPhoneNumber(), new CreateBurningRequest(BurningSourceType.DIRECT, "remember", null));

        assertEquals(BurningAnalysisStatus.PENDING, response.analysisStatus());
        verify(eventPublisher).publishEvent(any(BurningAnalysisRequestedEvent.class));
    }

    @Test
    void rejectsBurningDiaryTwice() {
        Member member = Member.builder().phoneNumber("01000000000").build();
        Diary diary = new Diary(member, "remember", DiaryEmotion.NORMAL);
        when(memberRepository.getReferenceById(member.getPhoneNumber())).thenReturn(member);
        when(diaryRepository.findByIdAndMemberPhoneNumber(1L, member.getPhoneNumber())).thenReturn(java.util.Optional.of(diary));
        when(burningRepository.existsByMemberPhoneNumberAndDiaryId(member.getPhoneNumber(), diary.getId())).thenReturn(true);

        BurningRequestException exception = assertThrows(BurningRequestException.class, () ->
                service.create(member.getPhoneNumber(), new CreateBurningRequest(BurningSourceType.DIARY, null, 1L)));
        assertEquals("BURN_409", exception.getErrorCode().getCode());
    }

    @Test
    void createsTalismanOnlyAfterCompletedAnalysis() {
        Member member = Member.builder().phoneNumber("01000000000").build();
        Burning burning = new Burning(member, BurningSourceType.DIRECT, null, "remember", LocalDateTime.now());
        BurningAnalysis analysis = new BurningAnalysis(burning);
        analysis.markProcessing(1);
        analysis.complete(1, new BurningAiResult("comment", "type", "CALM", "fake"));
        Talisman talisman = Talisman.builder().member(member).burnRitualId(1L).message("CALM").build();
        when(burningRepository.findByIdAndMemberPhoneNumber(1L, member.getPhoneNumber())).thenReturn(java.util.Optional.of(burning));
        when(analysisRepository.findByBurningIdForUpdate(1L)).thenReturn(java.util.Optional.of(analysis));
        when(talismanRepository.existsByBurnRitualId(1L)).thenReturn(false);
        when(talismanRepository.saveAndFlush(any(Talisman.class))).thenReturn(talisman);

        TalismanCreationResponse response = service.createTalisman(member.getPhoneNumber(), 1L);

        assertTrue(response.hasTalisman());
        assertEquals("CALM", response.talisman().message());
    }
}