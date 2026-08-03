package com.maumbujeok.backend.domain.burn.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "burning_analyses") @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BurningAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "burning_id", nullable = false, unique = true) private Burning burning;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private BurningAnalysisStatus status;
    @Column(nullable = false) private int inputRevision;
    @Column(name = "comment", columnDefinition = "TEXT") private String comment;
    @Column(name = "talisman_type", length = 50) private String talismanType;
    @Column(name = "talisman_text", length = 100) private String talismanText;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    public BurningAnalysis(Burning burning) { this.burning=burning; this.status=BurningAnalysisStatus.PENDING; this.inputRevision=1; }
}
