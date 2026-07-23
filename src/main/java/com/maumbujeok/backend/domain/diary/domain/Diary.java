package com.maumbujeok.backend.domain.diary.domain;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "selected_emotion", nullable = false, length = 30)
    @Convert(converter = DiaryEmotionConverter.class)
    private DiaryEmotion selectedEmotion;

    public Diary(Member member, String content, DiaryEmotion selectedEmotion) {
        this.member = member;
        this.content = content;
        this.selectedEmotion = selectedEmotion;
    }
}
