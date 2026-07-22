package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {
    static final String PROMPT_VERSION = "diary-v2";
    static final String POLICY_VERSION = "policy-v1";

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryAnalysisRepository analysisRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateDiaryResponse create(String memberPhoneNumber, CreateDiaryRequest request) {
        validate(request);
        Member member = memberRepository.getReferenceById(memberPhoneNumber);
        Diary diary = diaryRepository.save(new Diary(member, request.content().trim(), request.selectedEmotion().trim()));
        DiaryAnalysis analysis = analysisRepository.save(new DiaryAnalysis(diary, PROMPT_VERSION, POLICY_VERSION));
        log.info("Diary created diaryId={} analysisId={} status={} memberPhoneSuffix={}",
                diary.getId(), analysis.getId(), analysis.getStatus(), maskPhoneNumber(member.getPhoneNumber()));
        eventPublisher.publishEvent(new DiaryCreatedEvent(analysis.getId()));
        log.info("Diary analysis event published diaryId={} analysisId={}", diary.getId(), analysis.getId());
        return new CreateDiaryResponse(diary.getId(), analysis.getStatus());
    }

    @Transactional(readOnly = true)
    public DiaryAnalysisResponse getAnalysis(String memberPhoneNumber, Long diaryId) {
        DiaryAnalysis analysis = analysisRepository.findByDiaryIdAndDiaryMemberPhoneNumber(diaryId, memberPhoneNumber)
                .orElseThrow(() -> new DiaryRequestException("DIARY_404", "일기를 찾을 수 없습니다."));
        log.info("Diary analysis queried diaryId={} analysisId={} status={} model={} failureCode={}",
                diaryId, analysis.getId(), analysis.getStatus(), analysis.getModelName(), analysis.getFailureCode());
        return DiaryAnalysisResponse.from(analysis);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) return "****";
        return phoneNumber.substring(phoneNumber.length() - 4);
    }

    private void validate(CreateDiaryRequest request) {
        if (request == null || !StringUtils.hasText(request.content()) || request.content().length() > 5000) {
            throw new DiaryRequestException("DIARY_400", "일기 내용은 1자 이상 5000자 이하여야 합니다.");
        }
        if (!StringUtils.hasText(request.selectedEmotion()) || request.selectedEmotion().length() > 30) {
            throw new DiaryRequestException("DIARY_400", "선택 감정은 1자 이상 30자 이하여야 합니다.");
        }
    }
}
