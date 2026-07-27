package com.maumbujeok.backend.domain.diary.domain;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary_analysis", uniqueConstraints = @UniqueConstraint(name = "uk_diary_analysis_diary", columnNames = "diary_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryAnalysis extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiaryAnalysisStatus status;

    @Column(length = 40) private String summary;
    @Column(name = "empathy_response", length = 500) private String empathyResponse;
    @Column(name = "ai_negative_intensity") private Integer aiNegativeIntensity;
    @Column(name = "final_negative_intensity") private Integer finalNegativeIntensity;
    @Convert(converter = ReportEmotionConverter.class)
    @Column(name = "report_emotion", length = 30) private ReportEmotion reportEmotion;
    @Column(name = "salpuri_recommended") private Boolean salpuriRecommended;
    @Enumerated(EnumType.STRING)
    @Column(name = "safety_level", length = 20) private SafetyLevel safetyLevel;
    @Column(name = "model_name", length = 100) private String modelName;
    @Column(name = "prompt_version", nullable = false, length = 30) private String promptVersion;
    @Column(name = "policy_version", nullable = false, length = 30) private String policyVersion;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "failure_code", length = 50) private String failureCode;
    @Column(name = "analyzed_at") private LocalDateTime analyzedAt;

    public DiaryAnalysis(Diary diary, String promptVersion, String policyVersion) {
        this.diary = diary;
        this.status = DiaryAnalysisStatus.PENDING;
        this.promptVersion = promptVersion;
        this.policyVersion = policyVersion;
    }

    public void markProcessing() {
        if (status != DiaryAnalysisStatus.PENDING) {
            throw new IllegalStateException("Only pending analysis can start");
        }
        status = DiaryAnalysisStatus.PROCESSING;
    }

    public void complete(DiaryAiResult result, int finalScore, boolean recommended, int attempts, SafetyLevel safety) {
        applyResult(result, finalScore, recommended, attempts, safety);
        status = DiaryAnalysisStatus.COMPLETED;
        failureCode = null;
    }

    public void completeWithFallback(DiaryAiResult result, int finalScore, boolean recommended, int attempts, String code, SafetyLevel safety) {
        applyResult(result, finalScore, recommended, attempts, safety);
        status = DiaryAnalysisStatus.FALLBACK_COMPLETED;
        failureCode = code;
    }

    public void fail(int attempts, String code) {
        status = DiaryAnalysisStatus.FAILED;
        attemptCount = attempts;
        failureCode = code;
        analyzedAt = LocalDateTime.now();
    }

    private void applyResult(DiaryAiResult result, int finalScore, boolean recommended, int attempts, SafetyLevel safety) {
        if (status != DiaryAnalysisStatus.PROCESSING) {
            throw new IllegalStateException("Only processing analysis can complete");
        }
        summary = result.summary();
        empathyResponse = result.empathyResponse();
        aiNegativeIntensity = result.negativeIntensity();
        finalNegativeIntensity = finalScore;
        reportEmotion = result.reportEmotion();
        salpuriRecommended = recommended;
        safetyLevel = safety;
        modelName = result.modelName();
        attemptCount = attempts;
        analyzedAt = LocalDateTime.now();
    }
}
