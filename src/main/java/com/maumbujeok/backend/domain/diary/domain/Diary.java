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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "diaries",
        indexes = @Index(
                name = "idx_diaries_member_recorded_created",
                columnList = "member_phone_number, recorded_date, created_at, id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseTimeEntity {
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

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

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "burned_at")
    private LocalDateTime burnedAt;

    @Column(name = "burning_id")
    private Long burningId;

    public Diary(Member member, String content, DiaryEmotion selectedEmotion) {
        this(member, content, selectedEmotion, LocalDate.now(SERVICE_ZONE));
    }

    public Diary(Member member, String content, DiaryEmotion selectedEmotion, LocalDate recordedDate) {
        this.member = member;
        this.content = content;
        this.selectedEmotion = selectedEmotion;
        this.recordedDate = recordedDate;
    }

    public boolean update(String content, DiaryEmotion selectedEmotion) {
        if (this.content.equals(content) && this.selectedEmotion == selectedEmotion) {
            return false;
        }
        this.content = content;
        this.selectedEmotion = selectedEmotion;
        return true;
    }

    public boolean isBurned() { return burnedAt != null; }

    public void markBurned(Long burningId, LocalDateTime burnedAt) {
        this.burningId = burningId;
        this.burnedAt = burnedAt;
    }
}



