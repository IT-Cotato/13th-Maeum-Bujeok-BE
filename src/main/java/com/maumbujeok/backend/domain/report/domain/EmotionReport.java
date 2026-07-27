package com.maumbujeok.backend.domain.report.domain;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "emotion_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_emotion_report_member_type_period",
                columnNames = {"member_phone_number", "report_type", "period_start"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    private EmotionReportType reportType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 30)
    private EmotionReportGenerationStatus generationStatus;

    @Column(name = "generation_sequence", nullable = false)
    private int generationSequence;

    @Column(name = "insight_summary", length = 1200)
    private String insightSummary;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "prompt_version", nullable = false, length = 30)
    private String promptVersion;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    public EmotionReport(Member member, EmotionReportType reportType, LocalDate periodStart, LocalDate periodEnd, String promptVersion) {
        this.member = member;
        this.reportType = reportType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.promptVersion = promptVersion;
        this.generationStatus = EmotionReportGenerationStatus.PENDING;
        this.generationSequence = 1;
    }

    public void requestGeneration(LocalDate periodEnd, String promptVersion) {
        this.periodEnd = periodEnd;
        this.promptVersion = promptVersion;
        this.generationStatus = EmotionReportGenerationStatus.PENDING;
        this.generationSequence += 1;
        clearResult();
    }

    public boolean matches(int requestedSequence) {
        return generationSequence == requestedSequence;
    }

    public void markProcessing() {
        if (generationStatus != EmotionReportGenerationStatus.PENDING) {
            throw new IllegalStateException("Only pending reports can start generation");
        }
        generationStatus = EmotionReportGenerationStatus.PROCESSING;
    }

    public void complete(WeeklyReportAiResult result, int attempts) {
        applyResult(result, attempts);
        generationStatus = EmotionReportGenerationStatus.COMPLETED;
        failureCode = null;
    }

    public void completeWithFallback(WeeklyReportAiResult result, int attempts, String code) {
        applyResult(result, attempts);
        generationStatus = EmotionReportGenerationStatus.FALLBACK_COMPLETED;
        failureCode = code;
    }

    public void fail(int attempts, String code) {
        generationStatus = EmotionReportGenerationStatus.FAILED;
        attemptCount = attempts;
        failureCode = code;
        generatedAt = LocalDateTime.now();
    }

    private void applyResult(WeeklyReportAiResult result, int attempts) {
        if (generationStatus != EmotionReportGenerationStatus.PROCESSING) {
            throw new IllegalStateException("Only processing reports can complete");
        }
        insightSummary = result.insightSummary();
        modelName = result.modelName();
        attemptCount = attempts;
        generatedAt = LocalDateTime.now();
    }

    private void clearResult() {
        insightSummary = null;
        modelName = null;
        attemptCount = 0;
        failureCode = null;
        generatedAt = null;
    }
}
