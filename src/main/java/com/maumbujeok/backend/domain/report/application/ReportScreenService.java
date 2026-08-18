package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.burn.domain.Burning;
import com.maumbujeok.backend.domain.burn.repository.BurningAnalysisRepository;
import com.maumbujeok.backend.domain.burn.repository.BurningRepository;
import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowQueryResponse;
import com.maumbujeok.backend.domain.report.dto.ReportBurningItemResponse;
import com.maumbujeok.backend.domain.report.dto.ReportEmotionStatsResponse;
import com.maumbujeok.backend.domain.report.dto.ReportTalismansResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportScreenService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final EmotionReportRepository reportRepository;
    private final DiaryService diaryService;
    private final DiaryRepository diaryRepository;
    private final BurningRepository burningRepository;
    private final BurningAnalysisRepository burningAnalysisRepository;
    private final TalismanRepository talismanRepository;
    private final NextWeekFlowRepository nextWeekFlowRepository;
    private final NextWeekFlowService nextWeekFlowService;

    public EmotionReport ownedWeekly(String phone, Long reportId) {
        return reportRepository.findByIdAndMemberPhoneNumberAndReportType(reportId, phone, EmotionReportType.WEEKLY)
                .orElseThrow(() -> new ReportRequestException(ErrorCode.REPORT_NOT_FOUND, "Weekly report not found"));
    }

    public ReportEmotionStatsResponse emotionStats(String phone, Long reportId) {
        EmotionReport report = ownedWeekly(phone, reportId);
        return new ReportEmotionStatsResponse(report.getPeriodStart(), report.getPeriodEnd(),
                diaryService.getEmotionStats(phone, report.getPeriodStart(), report.getPeriodEnd()));
    }

    public List<ReportBurningItemResponse> burnings(String phone, Long reportId) {
        EmotionReport report = ownedWeekly(phone, reportId);
        LocalDateTime from = report.getPeriodStart().atStartOfDay(SEOUL).toLocalDateTime();
        LocalDateTime to = report.getPeriodEnd().plusDays(1).atStartOfDay(SEOUL).toLocalDateTime();
        return burningRepository.findAllByMemberPhoneNumberAndBurnedAtBetween(phone, from, to).stream()
                .map(burning -> toBurningItem(burning))
                .toList();
    }

    public ReportTalismansResponse talismans(String phone, Long reportId) {
        EmotionReport report = ownedWeekly(phone, reportId);
        List<TalismanItemResponse> items = talismanRepository
                .findAllByMemberPhoneNumberAndRecordedAtBetweenOrderByRecordedAtDescCreatedAtDesc(
                        phone, report.getPeriodStart(), report.getPeriodEnd())
                .stream().map(TalismanItemResponse::from).toList();
        return new ReportTalismansResponse(report.getPeriodStart(), report.getPeriodEnd(), items);
    }

    @Transactional
    public NextWeekFlowQueryResponse nextWeekFlow(String phone, Long reportId) {
        EmotionReport report = ownedWeekly(phone, reportId);

        long diaryCount = diaryRepository
                .countByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThan(
                        phone, report.getPeriodStart(), report.getPeriodEnd().plusDays(1));

        // 1. 일기 개수가 3개 미만이면 기존처럼 404 (FLOW_NOT_FOUND)를 반환하여 프론트에서 대체 텍스트 정상 표시
        if (diaryCount < 3) {
            throw new ReportRequestException(ErrorCode.FLOW_NOT_FOUND,
                    "Next week flow not found or insufficient diaries (less than 3)");
        }

        // 2. 3개 이상이면 즉시 PROCESSING 상태를 반환하거나 기존 완료된 흐름 리턴 (프론트 로딩 스피너 및 환각 방지)
        return nextWeekFlowService.getOrGenerateFlowForWeeklyReport(phone, report);
    }

    private ReportBurningItemResponse toBurningItem(Burning burning) {
        var analysis = burningAnalysisRepository.findByBurningId(burning.getId()).orElse(null);
        return new ReportBurningItemResponse(
                burning.getId(), burning.getTitle(), burning.getSourceType(), burning.getBurnedAt(),
                analysis == null ? null : analysis.getStatus(),
                analysis != null && analysis.getTalismanType() != null);
    }
}