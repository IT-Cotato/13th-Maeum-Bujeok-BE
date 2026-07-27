package com.maumbujeok.backend.domain.upload.domain;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary_uploads", indexes = {
        @Index(name = "idx_diary_uploads_member_state", columnList = "member_phone_number,state"),
        @Index(name = "idx_diary_uploads_diary_position", columnList = "diary_id,sort_order")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryUpload extends BaseTimeEntity {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_phone_number", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;

    @Column(name = "object_key", nullable = false, unique = true, length = 200)
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UploadState state;

    public DiaryUpload(UUID id, Member member, String objectKey, String contentType, long fileSize) {
        this.id = id;
        this.member = member;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.state = UploadState.PENDING;
    }

    public boolean canAttachTo(Diary target) {
        return state == UploadState.PENDING
                || (state == UploadState.ATTACHED && diary != null && diary.getId().equals(target.getId()));
    }

    public void attach(Diary target, int order) {
        this.diary = target;
        this.sortOrder = order;
        this.state = UploadState.ATTACHED;
    }

    public void markDeletionPending() {
        this.diary = null;
        this.sortOrder = null;
        this.state = UploadState.DELETION_PENDING;
    }
}
