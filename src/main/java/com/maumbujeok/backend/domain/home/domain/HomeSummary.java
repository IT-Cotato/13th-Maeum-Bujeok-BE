package com.maumbujeok.backend.domain.home.domain;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "home_summaries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_home_summary_member_date", columnNames = {"member_phone_number", "summary_date"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeSummary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "home_summary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_element", nullable = false, length = 20)
    private PrimaryElement primaryElement;

    @Column(name = "today_luck", nullable = false, length = 500)
    private String todayLuck;

    @Column(name = "today_energy", nullable = false, length = 1000)
    private String todayEnergy;

    @Column(name = "model_name", length = 50)
    private String modelName;

    @Column(name = "report_version", length = 20)
    private String reportVersion;

    @Builder
    public HomeSummary(Member member, LocalDate summaryDate, PrimaryElement primaryElement,
                       String todayLuck, String todayEnergy, String modelName, String reportVersion) {
        this.member = member;
        this.summaryDate = summaryDate;
        this.primaryElement = primaryElement;
        this.todayLuck = todayLuck;
        this.todayEnergy = todayEnergy;
        this.modelName = modelName;
        this.reportVersion = reportVersion;
    }

    public void update(PrimaryElement primaryElement, String todayLuck, String todayEnergy, String modelName, String reportVersion) {
        this.primaryElement = primaryElement;
        this.todayLuck = todayLuck;
        this.todayEnergy = todayEnergy;
        this.modelName = modelName;
        this.reportVersion = reportVersion;
    }
}
