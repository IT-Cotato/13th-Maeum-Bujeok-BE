package com.maumbujeok.backend.domain.burn.domain;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "burnings", uniqueConstraints = @UniqueConstraint(name = "uk_burnings_member_diary", columnNames = {"member_phone_number", "diary_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Burning extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_phone_number", nullable = false) private Member member;
    @Enumerated(EnumType.STRING) @Column(name = "source_type", nullable = false, length = 20) private BurningSourceType sourceType;
    @Column(name = "diary_id") private Long diaryId;
    @Column(name = "source_content", nullable = false, columnDefinition = "TEXT") private String sourceContent;
    @Column(length = 100) private String title;
    public void setAiTitle(String title) { this.title = title; }
    @Column(name = "burned_at", nullable = false) private LocalDateTime burnedAt;
    public Burning(Member member, BurningSourceType sourceType, Long diaryId, String sourceContent, LocalDateTime burnedAt) { this.member=member; this.sourceType=sourceType; this.diaryId=diaryId; this.sourceContent=sourceContent; this.burnedAt=burnedAt; }
}
