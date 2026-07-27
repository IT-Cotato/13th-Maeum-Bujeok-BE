package com.maumbujeok.backend.domain.report.domain;

import com.maumbujeok.backend.domain.member.domain.Member;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "next_week_flows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NextWeekFlow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emotion_report_id", nullable = false)
    private EmotionReport emotionReport;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 30)
    private NextWeekFlowGenerationStatus generationStatus;

    @Column(name = "advice_text", length = 3000)
    private String adviceText;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "report_version", length = 30)
    private String reportVersion;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Builder
    public NextWeekFlow(Member member, EmotionReport emotionReport, LocalDate weekStart, LocalDate periodStart, LocalDate periodEnd, NextWeekFlowGenerationStatus generationStatus) {
        this.member = member;
        this.emotionReport = emotionReport;
        this.weekStart = weekStart;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.generationStatus = generationStatus != null ? generationStatus : NextWeekFlowGenerationStatus.PROCESSING;
    }

    public void complete(String adviceText, String modelName, String reportVersion) {
        this.adviceText = adviceText;
        this.modelName = modelName;
        this.reportVersion = reportVersion;
        this.generationStatus = NextWeekFlowGenerationStatus.COMPLETED;
        this.generatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.generationStatus = NextWeekFlowGenerationStatus.FAILED;
        this.generatedAt = LocalDateTime.now();
    }
}
