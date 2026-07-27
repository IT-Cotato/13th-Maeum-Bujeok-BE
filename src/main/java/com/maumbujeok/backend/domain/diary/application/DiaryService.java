package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryEmotionCountProjection;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final List<DiaryAnalysisStatus> GRAPH_STATUSES = List.of(
            DiaryAnalysisStatus.COMPLETED,
            DiaryAnalysisStatus.FALLBACK_COMPLETED
    );

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryAnalysisRepository analysisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock serviceClock;

    @Transactional
    public CreateDiaryResponse create(String memberPhoneNumber, CreateDiaryRequest request) {
        validateCreate(request);
        LocalDate recordedDate = request.recordedDate() == null
                ? LocalDate.now(serviceClock)
                : request.recordedDate();
        validateRecordedDate(recordedDate);

        Member member = memberRepository.getReferenceById(memberPhoneNumber);
        DiaryEmotion selectedEmotion = parseEmotion(request.selectedEmotion());
        Diary diary = diaryRepository.save(
                new Diary(member, request.content().trim(), selectedEmotion, recordedDate)
        );
        DiaryAnalysis analysis = analysisRepository.save(
                new DiaryAnalysis(diary, PROMPT_VERSION, POLICY_VERSION)
        );

        eventPublisher.publishEvent(
                new DiaryAnalysisRequestedEvent(analysis.getId(), analysis.getInputRevision())
        );
        log.info(
                "Diary created diaryId={} analysisId={} inputRevision={} status={} memberPhoneSuffix={}",
                diary.getId(),
                analysis.getId(),
                analysis.getInputRevision(),
                analysis.getStatus(),
                maskPhoneNumber(member.getPhoneNumber())
        );
        return new CreateDiaryResponse(diary.getId(), diary.getRecordedDate(), analysis.getStatus());
    }

    @Transactional(readOnly = true)
    public DiaryResponse get(String memberPhoneNumber, Long diaryId) {
        Diary diary = diaryRepository.findByIdAndMemberPhoneNumber(diaryId, memberPhoneNumber)
                .orElseThrow(this::diaryNotFound);
        return DiaryResponse.from(diary);
    }

    @Transactional(readOnly = true)
    public DiaryAnalysisResponse getAnalysis(String memberPhoneNumber, Long diaryId) {
        DiaryAnalysis analysis = analysisRepository.findByDiaryIdAndDiaryMemberPhoneNumber(
                        diaryId,
                        memberPhoneNumber
                )
                .orElseThrow(this::diaryNotFound);
        log.info(
                "Diary analysis queried diaryId={} analysisId={} inputRevision={} status={} model={} failureCode={}",
                diaryId,
                analysis.getId(),
                analysis.getInputRevision(),
                analysis.getStatus(),
                analysis.getModelName(),
                analysis.getFailureCode()
        );
        return DiaryAnalysisResponse.from(analysis);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getAll(String memberPhoneNumber) {
        return getAll(memberPhoneNumber, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getAll(
            String memberPhoneNumber,
            LocalDate date,
            Integer year,
            Integer month
    ) {
        List<Diary> diaries;
        if (date != null) {
            if (year != null || month != null) {
                throw invalidRequest("date와 year/month는 함께 사용할 수 없습니다.");
            }
            diaries = diaryRepository.findAllByMemberPhoneNumberAndRecordedDateOrderByCreatedAtDescIdDesc(
                    memberPhoneNumber,
                    date
            );
        } else if (year != null || month != null) {
            if (year == null || month == null) {
                throw invalidRequest("월별 조회에는 year와 month가 모두 필요합니다.");
            }
            YearMonth yearMonth;
            try {
                yearMonth = YearMonth.of(year, month);
            } catch (RuntimeException exception) {
                throw invalidRequest("조회 연도 또는 월이 올바르지 않습니다.");
            }
            diaries = diaryRepository
                    .findAllByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThanOrderByRecordedDateDescCreatedAtDescIdDesc(
                            memberPhoneNumber,
                            yearMonth.atDay(1),
                            yearMonth.plusMonths(1).atDay(1)
                    );
        } else {
            diaries = diaryRepository.findAllByMemberPhoneNumberOrderByRecordedDateDescCreatedAtDescIdDesc(
                    memberPhoneNumber
            );
        }

        return diaries.stream().map(DiaryResponse::from).toList();
    }

    @Transactional
    public UpdateDiaryResponse update(
            String memberPhoneNumber,
            Long diaryId,
            UpdateDiaryRequest request
    ) {
        validateUpdate(request);
        DiaryAnalysis analysis = analysisRepository.findOwnedByDiaryIdForUpdate(
                        diaryId,
                        memberPhoneNumber
                )
                .orElseThrow(this::diaryNotFound);
        Diary diary = analysis.getDiary();

        String content = request.content() == null
                ? diary.getContent()
                : request.content().trim();
        DiaryEmotion selectedEmotion = request.selectedEmotion() == null
                ? diary.getSelectedEmotion()
                : parseEmotion(request.selectedEmotion());

        boolean changed = diary.update(content, selectedEmotion);
        if (!changed) {
            return UpdateDiaryResponse.from(diary, analysis.getStatus(), false);
        }

        long inputRevision = analysis.restart();
        diaryRepository.flush();
        analysisRepository.flush();
        eventPublisher.publishEvent(
                new DiaryAnalysisRequestedEvent(analysis.getId(), inputRevision)
        );
        log.info(
                "Diary updated diaryId={} analysisId={} inputRevision={} memberPhoneSuffix={}",
                diaryId,
                analysis.getId(),
                inputRevision,
                maskPhoneNumber(memberPhoneNumber)
        );
        return UpdateDiaryResponse.from(diary, analysis.getStatus(), true);
    }

    @Transactional(readOnly = true)
    public List<EmotionStatResponse> getEmotionStats(
            String memberPhoneNumber,
            LocalDate from,
            LocalDate to
    ) {
        if (from == null || to == null || from.isAfter(to)) {
            throw invalidRequest("감정 통계 조회에는 올바른 from과 to 날짜가 필요합니다.");
        }

        Map<ReportEmotion, Long> counts = new EnumMap<>(ReportEmotion.class);
        for (ReportEmotion emotion : ReportEmotion.values()) counts.put(emotion, 0L);
        for (DiaryEmotionCountProjection projection : analysisRepository.countReportEmotions(
                memberPhoneNumber,
                from,
                to,
                GRAPH_STATUSES
        )) {
            counts.put(projection.getEmotion(), projection.getCount());
        }
        return counts.entrySet().stream()
                .map(entry -> new EmotionStatResponse(entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }

    private void validateCreate(CreateDiaryRequest request) {
        if (request == null) throw invalidRequest("일기 요청이 필요합니다.");
        validateContent(request.content());
        validateEmotionValue(request.selectedEmotion());
    }

    private void validateUpdate(UpdateDiaryRequest request) {
        if (request == null || (request.content() == null && request.selectedEmotion() == null)) {
            throw invalidRequest("수정할 일기 내용 또는 선택 감정이 필요합니다.");
        }
        if (request.content() != null) validateContent(request.content());
        if (request.selectedEmotion() != null) validateEmotionValue(request.selectedEmotion());
    }

    private void validateContent(String content) {
        if (!StringUtils.hasText(content) || content.length() > MAX_CONTENT_LENGTH) {
            throw invalidRequest("일기 내용은 1자 이상 5000자 이하여야 합니다.");
        }
    }

    private void validateEmotionValue(String value) {
        if (!StringUtils.hasText(value) || value.length() > 30) {
            throw invalidRequest("선택 감정은 필수입니다.");
        }
    }

    private void validateRecordedDate(LocalDate recordedDate) {
        if (recordedDate.isAfter(LocalDate.now(serviceClock))) {
            throw invalidRequest("미래 날짜의 일기는 작성할 수 없습니다.");
        }
    }

    private DiaryEmotion parseEmotion(String value) {
        try {
            return DiaryEmotion.fromInput(value);
        } catch (IllegalArgumentException exception) {
            throw invalidRequest(
                    "선택 감정은 다음 9개 코드 중 하나여야 합니다: "
                            + String.join(", ", DiaryEmotion.codes())
            );
        }
    }

    private DiaryRequestException diaryNotFound() {
        return new DiaryRequestException(ErrorCode.DIARY_NOT_FOUND, "일기를 찾을 수 없습니다.");
    }

    private DiaryRequestException invalidRequest(String message) {
        return new DiaryRequestException(ErrorCode.INVALID_DIARY_REQUEST, message);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) return "****";
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}
