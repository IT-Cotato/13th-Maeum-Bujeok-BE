package com.maumbujeok.backend.domain.talisman.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "talismans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Talisman extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @Column(name = "burn_ritual_id")
    private Long burnRitualId;

    @Column(name = "design_type", length = 50)
    private String designType;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "used_saju", length = 500)
    private String usedSaju;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 30)
    private TalismanGenerationStatus generationStatus;

    @Builder
    public Talisman(Member member, Long burnRitualId, String designType, String title, String message, String imageUrl, String usedSaju, TalismanGenerationStatus generationStatus) {
        this.member = member;
        this.burnRitualId = burnRitualId;
        this.designType = designType;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.usedSaju = usedSaju;
        this.generationStatus = generationStatus != null ? generationStatus : TalismanGenerationStatus.COMPLETED;
    }
}
