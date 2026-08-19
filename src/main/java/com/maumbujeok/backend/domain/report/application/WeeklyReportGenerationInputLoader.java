package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class WeeklyReportGenerationInputLoader {
    private static final EnumSet<DiaryEmotion> POSITIVE_EMOTIONS = EnumSet.of(
            DiaryEmotion.JOYFUL, DiaryEmotion.HAPPY, DiaryEmotion.EXCITED, DiaryEmotion.COMFORTABLE
    );
    private static final EnumSet<DiaryEmotion> NEGATIVE_EMOTIONS = EnumSet.of(
            DiaryEmotion.LETHARGIC, DiaryEmotion.SAD, DiaryEmotion.ANXIOUS, DiaryEmotion.ANGRY
    );

    private final EmotionReportRepository reportRepository;
    private final DiaryRepository diaryRepository;

    @Transactional(readOnly = true)
    public WeeklyReportGenerationInput load(Long reportId) {
        EmotionReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        LocalDate periodStart = report.getPeriodStart();
        LocalDate periodEndExclusive = report.getPeriodEnd().plusDays(1);
        List<Diary> diaries = diaryRepository
                .findAllByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThanAndBurnedAtIsNullOrderByRecordedDateAscIdAsc(
                        report.getMember().getPhoneNumber(),
                        periodStart,
                        periodEndExclusive
                );

        if (diaries.isEmpty()) {
            throw new IllegalArgumentException("No diary entries found for the requested report period");
        }

        return new WeeklyReportGenerationInput(
                report.getId(),
                report.getGenerationSequence(),
                resolveDisplayName(report.getMember().getName()),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                diaries.stream().map(this::toDiaryEntry).toList(),
                snapshot(diaries)
        );
    }

    private WeeklyDiaryEntry toDiaryEntry(Diary diary) {
        return new WeeklyDiaryEntry(
                diary.getRecordedDate(),
                diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(),
                diary.getContent()
        );
    }

    private WeeklyEmotionSnapshot snapshot(List<Diary> diaries) {
        Map<DiaryEmotion, Integer> counts = new EnumMap<>(DiaryEmotion.class);
        Arrays.stream(DiaryEmotion.values()).forEach(emotion -> counts.put(emotion, 0));
        diaries.forEach(diary -> counts.computeIfPresent(diary.getSelectedEmotion(), (emotion, count) -> count + 1));

        int total = diaries.size();
        int positive = POSITIVE_EMOTIONS.stream().mapToInt(emotion -> counts.getOrDefault(emotion, 0)).sum();
        int negative = NEGATIVE_EMOTIONS.stream().mapToInt(emotion -> counts.getOrDefault(emotion, 0)).sum();
        int neutral = total - positive - negative;

        String dominantEmotionLabel = counts.entrySet().stream()
                .sorted((left, right) -> {
                    int byCount = Integer.compare(right.getValue(), left.getValue());
                    return byCount != 0 ? byCount : Integer.compare(left.getKey().ordinal(), right.getKey().ordinal());
                })
                .map(entry -> entry.getKey().getLabel())
                .findFirst()
                .orElse(DiaryEmotion.NORMAL.getLabel());

        List<WeeklyEmotionCount> emotionCounts = Arrays.stream(DiaryEmotion.values())
                .map(emotion -> new WeeklyEmotionCount(
                        emotion.name(),
                        emotion.getLabel(),
                        counts.getOrDefault(emotion, 0),
                        ratio(counts.getOrDefault(emotion, 0), total),
                        polarityOf(emotion)
                ))
                .toList();

        return new WeeklyEmotionSnapshot(
                total,
                ratio(positive, total),
                ratio(neutral, total),
                ratio(negative, total),
                dominantEmotionLabel,
                emotionCounts
        );
    }

    private double ratio(int count, int total) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf((count * 100.0) / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String polarityOf(DiaryEmotion emotion) {
        if (POSITIVE_EMOTIONS.contains(emotion)) {
            return "POSITIVE";
        }
        if (NEGATIVE_EMOTIONS.contains(emotion)) {
            return "NEGATIVE";
        }
        return "NEUTRAL";
    }

    private String resolveDisplayName(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return "마음님";
        }
        String normalized = memberName.trim();
        return normalized.endsWith("님") ? normalized : normalized + "님";
    }
}
