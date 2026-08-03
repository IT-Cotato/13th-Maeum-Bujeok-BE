package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCalendarDayResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCalendarResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCursorPageResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryDetailResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryEmotionCountProjection;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.upload.application.UploadService;
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
import org.springframework.data.domain.PageRequest;
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
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final List<DiaryAnalysisStatus> GRAPH_STATUSES = List.of(
            DiaryAnalysisStatus.COMPLETED, DiaryAnalysisStatus.FALLBACK_COMPLETED);

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryAnalysisRepository analysisRepository;
    private final UploadService uploadService;
    private final DiaryCursorCodec cursorCodec;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock serviceClock;

    @Transactional
    public CreateDiaryResponse create(String phoneNumber, CreateDiaryRequest request) {
        validateCreate(request);
        LocalDate recordedDate = request.recordedDate() == null
                ? LocalDate.now(serviceClock) : request.recordedDate();
        validateRecordedDate(recordedDate);
        Member member = memberRepository.getReferenceById(phoneNumber);
        Diary diary = diaryRepository.saveAndFlush(new Diary(
                member, request.content().trim(), parseEmotion(request.selectedEmotion()), recordedDate));
        DiaryAnalysis analysis = analysisRepository.save(
                new DiaryAnalysis(diary, PROMPT_VERSION, POLICY_VERSION));
        uploadService.syncAttachments(phoneNumber, diary, request.imageUploadIds());
        log.info("Diary created diaryId={} analysisId={} status={} memberPhoneSuffix={}",
                diary.getId(), analysis.getId(), analysis.getStatus(), maskPhoneNumber(phoneNumber));
        eventPublisher.publishEvent(
                new DiaryAnalysisRequestedEvent(analysis.getId(), analysis.getInputRevision()));
        eventPublisher.publishEvent(
                new DiaryWeeklyReportRefreshRequestedEvent(phoneNumber, diary.getRecordedDate()));
        log.info("Diary analysis and weekly report refresh events published diaryId={} analysisId={}",
                diary.getId(), analysis.getId());
        return new CreateDiaryResponse(diary.getId(), diary.getRecordedDate(), analysis.getStatus());
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponse get(String phoneNumber, Long diaryId) {
        DiaryAnalysis analysis = analysisRepository.findByDiaryIdAndDiaryMemberPhoneNumber(
                        diaryId, phoneNumber)
                .orElseThrow(this::diaryNotFound);
        return DiaryDetailResponse.from(
                analysis.getDiary(), analysis, uploadService.getImages(diaryId));
    }

    @Transactional(readOnly = true)
    public DiaryAnalysisResponse getAnalysis(String phoneNumber, Long diaryId) {
        DiaryAnalysis analysis = analysisRepository.findByDiaryIdAndDiaryMemberPhoneNumber(
                        diaryId, phoneNumber)
                .orElseThrow(this::diaryNotFound);
        return DiaryAnalysisResponse.from(analysis);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getByDate(String phoneNumber, LocalDate date) {
        if (date == null) {
            throw invalidRequest("date is required");
        }
        return diaryRepository.findAllByMemberPhoneNumberAndRecordedDateOrderByCreatedAtDescIdDesc(
                        phoneNumber, date)
                .stream().map(DiaryResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public DiaryCursorPageResponse getPage(String phoneNumber, String cursor, Integer requestedSize) {
        int size = requestedSize == null ? DEFAULT_PAGE_SIZE : requestedSize;
        if (size < 1 || size > MAX_PAGE_SIZE) throw invalidRequest("size must be between 1 and 50");
        DiaryCursorCodec.DiaryCursor decoded = cursorCodec.decode(cursor);
        List<Diary> fetched = diaryRepository.findCursor(
                phoneNumber, decoded.recordedDate(), decoded.diaryId(), PageRequest.of(0, size + 1));
        boolean hasNext = fetched.size() > size;
        List<Diary> page = hasNext ? fetched.subList(0, size) : fetched;
        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            Diary last = page.get(page.size() - 1);
            nextCursor = cursorCodec.encode(last.getRecordedDate(), last.getId());
        }
        return new DiaryCursorPageResponse(
                page.stream().map(DiaryResponse::from).toList(), nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public DiaryCalendarResponse getCalendar(String phoneNumber, Integer year, Integer month) {
        YearMonth target = parseYearMonth(year, month);
        List<DiaryCalendarDayResponse> days = findMonth(phoneNumber, target).stream()
                .map(diary -> new DiaryCalendarDayResponse(
                        diary.getRecordedDate(), "STORED", diary.getId(), null))
                .toList();
        return new DiaryCalendarResponse(target.getYear(), target.getMonthValue(), days);
    }

    @Transactional
    public UpdateDiaryResponse update(String phoneNumber, Long diaryId, UpdateDiaryRequest request) {
        validateUpdate(request);
        DiaryAnalysis analysis = analysisRepository.findOwnedByDiaryIdForUpdate(diaryId, phoneNumber)
                .orElseThrow(this::diaryNotFound);
        Diary diary = analysis.getDiary();
        if (diary.isBurned()) throw new DiaryRequestException(ErrorCode.INVALID_DIARY_REQUEST, "Burned diary cannot be updated");
        String content = request.content() == null ? diary.getContent() : request.content().trim();
        DiaryEmotion emotion = request.selectedEmotion() == null
                ? diary.getSelectedEmotion() : parseEmotion(request.selectedEmotion());

        boolean analysisChanged = diary.update(content, emotion);
        uploadService.syncAttachments(phoneNumber, diary, request.imageUploadIds());
        if (!analysisChanged) {
            return UpdateDiaryResponse.from(diary, analysis.getStatus(), false);
        }
        long revision = analysis.restart();
        diaryRepository.flush();
        analysisRepository.flush();
        eventPublisher.publishEvent(new DiaryAnalysisRequestedEvent(analysis.getId(), revision));
        return UpdateDiaryResponse.from(diary, analysis.getStatus(), true);
    }

    @Transactional
    public void delete(String phoneNumber, Long diaryId) {
        DiaryAnalysis analysis = analysisRepository.findOwnedByDiaryIdForUpdate(diaryId, phoneNumber)
                .orElseThrow(this::diaryNotFound);
        Diary diary = analysis.getDiary();
        if (diary.isBurned()) throw new DiaryRequestException(ErrorCode.INVALID_DIARY_REQUEST, "Burned diary cannot be deleted");
        uploadService.markDiaryImagesForDeletion(diaryId);
        analysisRepository.delete(analysis);
        diaryRepository.delete(diary);
    }

    @Transactional(readOnly = true)
    public List<EmotionStatResponse> getEmotionStats(String phoneNumber, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw invalidRequest("Valid from and to dates are required");
        }
        Map<ReportEmotion, Long> counts = new EnumMap<>(ReportEmotion.class);
        for (ReportEmotion emotion : ReportEmotion.values()) counts.put(emotion, 0L);
        for (DiaryEmotionCountProjection projection : analysisRepository.countReportEmotions(
                phoneNumber, from, to, GRAPH_STATUSES)) {
            counts.put(projection.getEmotion(), projection.getCount());
        }
        return counts.entrySet().stream()
                .map(entry -> new EmotionStatResponse(entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }

    private List<Diary> findMonth(String phoneNumber, YearMonth target) {
        return diaryRepository
                .findAllByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThanOrderByRecordedDateDescCreatedAtDescIdDesc(
                        phoneNumber, target.atDay(1), target.plusMonths(1).atDay(1));
    }

    private YearMonth parseYearMonth(Integer year, Integer month) {
        if (year == null || month == null) throw invalidRequest("year and month are required");
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException exception) {
            throw invalidRequest("Invalid year or month");
        }
    }

    private void validateCreate(CreateDiaryRequest request) {
        if (request == null) throw invalidRequest("Diary request is required");
        validateContent(request.content());
        validateEmotionValue(request.selectedEmotion());
    }

    private void validateUpdate(UpdateDiaryRequest request) {
        if (request == null || (request.content() == null
                && request.selectedEmotion() == null && request.imageUploadIds() == null)) {
            throw invalidRequest("At least one field must be updated");
        }
        if (request.content() != null) validateContent(request.content());
        if (request.selectedEmotion() != null) validateEmotionValue(request.selectedEmotion());
    }

    private void validateContent(String content) {
        if (!StringUtils.hasText(content) || content.length() > MAX_CONTENT_LENGTH) {
            throw invalidRequest("Diary content must contain 1 to 5000 characters");
        }
    }

    private void validateEmotionValue(String value) {
        if (!StringUtils.hasText(value) || value.length() > 30) {
            throw invalidRequest("Selected emotion is required");
        }
    }

    private void validateRecordedDate(LocalDate date) {
        if (date.isAfter(LocalDate.now(serviceClock))) {
            throw invalidRequest("A future diary cannot be created");
        }
    }

    private DiaryEmotion parseEmotion(String value) {
        try {
            return DiaryEmotion.fromInput(value);
        } catch (IllegalArgumentException exception) {
            throw invalidRequest("Invalid selected emotion");
        }
    }

    private DiaryRequestException diaryNotFound() {
        return new DiaryRequestException(ErrorCode.DIARY_NOT_FOUND, "Diary not found");
    }

    private DiaryRequestException invalidRequest(String message) {
        return new DiaryRequestException(ErrorCode.INVALID_DIARY_REQUEST, message);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}


