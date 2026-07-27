package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.List;
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
        DiaryEmotion selectedEmotion = parseEmotion(request.selectedEmotion());
        Diary diary = diaryRepository.save(new Diary(member, request.content().trim(), selectedEmotion));
        DiaryAnalysis analysis = analysisRepository.save(new DiaryAnalysis(diary, PROMPT_VERSION, POLICY_VERSION));
        log.info("Diary created diaryId={} analysisId={} status={} memberPhoneSuffix={}",
                diary.getId(), analysis.getId(), analysis.getStatus(), maskPhoneNumber(member.getPhoneNumber()));
        eventPublisher.publishEvent(new DiaryCreatedEvent(analysis.getId()));
        LocalDate diaryDate = diary.getCreatedAt() == null ? LocalDate.now() : diary.getCreatedAt().toLocalDate();
        eventPublisher.publishEvent(new DiaryWeeklyReportRefreshRequestedEvent(memberPhoneNumber, diaryDate));
        log.info("Diary analysis event published diaryId={} analysisId={}", diary.getId(), analysis.getId());
        return new CreateDiaryResponse(diary.getId(), analysis.getStatus());
    }

    @Transactional(readOnly = true)
    public DiaryAnalysisResponse getAnalysis(String memberPhoneNumber, Long diaryId) {
        DiaryAnalysis analysis = analysisRepository.findByDiaryIdAndDiaryMemberPhoneNumber(diaryId, memberPhoneNumber)
                .orElseThrow(() -> new DiaryRequestException(ErrorCode.DIARY_NOT_FOUND, "일기를 찾을 수 없습니다."));
        log.info("Diary analysis queried diaryId={} analysisId={} status={} model={} failureCode={}",
                diaryId, analysis.getId(), analysis.getStatus(), analysis.getModelName(), analysis.getFailureCode());
        return DiaryAnalysisResponse.from(analysis);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getAll(String memberPhoneNumber) {
        return diaryRepository.findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(memberPhoneNumber)
                .stream()
                .map(DiaryResponse::from)
                .toList();
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) return "****";
        return phoneNumber.substring(phoneNumber.length() - 4);
    }

    private void validate(CreateDiaryRequest request) {
        if (request == null || !StringUtils.hasText(request.content()) || request.content().length() > 5000) {
            throw new DiaryRequestException(ErrorCode.INVALID_DIARY_REQUEST, "일기 내용은 1자 이상 5000자 이하여야 합니다.");
        }
        if (!StringUtils.hasText(request.selectedEmotion()) || request.selectedEmotion().length() > 30) {
            throw new DiaryRequestException(ErrorCode.INVALID_DIARY_REQUEST, "선택 감정은 필수입니다.");
        }
    }

    private DiaryEmotion parseEmotion(String value) {
        try {
            return DiaryEmotion.fromInput(value);
        } catch (IllegalArgumentException exception) {
            throw new DiaryRequestException(
                    ErrorCode.INVALID_DIARY_REQUEST,
                    "선택 감정은 다음 9개 코드 중 하나여야 합니다: " + String.join(", ", DiaryEmotion.codes())
            );
        }
    }
}
