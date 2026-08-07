package com.maumbujeok.backend.domain.saju.domain;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.saju.ai.SajuAiResult;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "saju_analyses",
        indexes = @Index(name = "idx_saju_analyses_member_created", columnList = "member_phone_number, created_at, saju_analysis_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SajuAnalysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saju_analysis_id")
    private Long id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SajuAnalysisStatus status;

    @Column(name = "birth_date", nullable = false, length = 8)
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberSajuProfile.Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", nullable = false, length = 20)
    private MemberSajuProfile.CalendarType calendarType;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "wood_percentage")
    private Integer woodPercentage;

    @Column(name = "fire_percentage")
    private Integer firePercentage;

    @Column(name = "earth_percentage")
    private Integer earthPercentage;

    @Column(name = "metal_percentage")
    private Integer metalPercentage;

    @Column(name = "water_percentage")
    private Integer waterPercentage;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "prompt_version", nullable = false, length = 30)
    private String promptVersion;

    @Column(name = "request_sequence", nullable = false)
    private long requestSequence;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    public SajuAnalysis(
            Member member,
            String birthDate,
            MemberSajuProfile.Gender gender,
            MemberSajuProfile.CalendarType calendarType,
            LocalTime birthTime,
            String promptVersion
    ) {
        this.member = member;
        this.birthDate = birthDate;
        this.gender = gender;
        this.calendarType = calendarType;
        this.birthTime = birthTime;
        this.promptVersion = promptVersion;
        this.status = SajuAnalysisStatus.PENDING;
        this.requestSequence = 1L;
    }

    public boolean markProcessing(long expectedSequence) {
        if (requestSequence != expectedSequence || status != SajuAnalysisStatus.PENDING) {
            return false;
        }
        status = SajuAnalysisStatus.PROCESSING;
        return true;
    }

    public boolean complete(long expectedSequence, SajuAiResult result, int attempts) {
        if (!canFinish(expectedSequence)) {
            return false;
        }
        woodPercentage = result.woodPercentage();
        firePercentage = result.firePercentage();
        earthPercentage = result.earthPercentage();
        metalPercentage = result.metalPercentage();
        waterPercentage = result.waterPercentage();
        modelName = result.modelName();
        attemptCount = Math.max(attempts, 1);
        failureCode = null;
        analyzedAt = LocalDateTime.now();
        status = SajuAnalysisStatus.COMPLETED;
        return true;
    }

    public boolean fail(long expectedSequence, int attempts, String code) {
        if (!canFinish(expectedSequence)) {
            return false;
        }
        attemptCount = Math.max(attempts, 1);
        failureCode = (code == null || code.isBlank()) ? "AI_PROVIDER_ERROR" : code;
        if (!hasVisibleResult()) {
            analyzedAt = LocalDateTime.now();
        }
        status = SajuAnalysisStatus.FAILED;
        return true;
    }

    public boolean matchesInput(
            String birthDate,
            MemberSajuProfile.Gender gender,
            MemberSajuProfile.CalendarType calendarType,
            LocalTime birthTime
    ) {
        return Objects.equals(this.birthDate, birthDate)
                && this.gender == gender
                && this.calendarType == calendarType
                && Objects.equals(this.birthTime, birthTime);
    }

    public boolean hasVisibleResult() {
        return woodPercentage != null
                && firePercentage != null
                && earthPercentage != null
                && metalPercentage != null
                && waterPercentage != null;
    }

    public long requestAnalysis(
            String birthDate,
            MemberSajuProfile.Gender gender,
            MemberSajuProfile.CalendarType calendarType,
            LocalTime birthTime,
            String promptVersion
    ) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.calendarType = calendarType;
        this.birthTime = birthTime;
        this.promptVersion = promptVersion;
        this.requestSequence += 1;
        this.status = SajuAnalysisStatus.PENDING;
        this.attemptCount = 0;
        this.failureCode = null;

        if (!hasVisibleResult()) {
            this.modelName = null;
            this.analyzedAt = null;
        }
        return requestSequence;
    }

    private boolean canFinish(long expectedSequence) {
        return requestSequence == expectedSequence && status == SajuAnalysisStatus.PROCESSING;
    }
}
