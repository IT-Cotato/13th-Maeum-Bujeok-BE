package com.maumbujeok.backend.domain.upload.repository;

import com.maumbujeok.backend.domain.upload.domain.DiaryUpload;
import com.maumbujeok.backend.domain.upload.domain.UploadState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryUploadRepository extends JpaRepository<DiaryUpload, UUID> {
    Optional<DiaryUpload> findByIdAndMemberPhoneNumber(UUID id, String phoneNumber);

    List<DiaryUpload> findAllByMemberPhoneNumber(String phoneNumber);

    List<DiaryUpload> findAllByIdInAndMemberPhoneNumber(List<UUID> ids, String phoneNumber);

    List<DiaryUpload> findAllByDiaryIdAndStateOrderBySortOrderAsc(Long diaryId, UploadState state);

    List<DiaryUpload> findTop100ByStateOrderByCreatedAtAsc(UploadState state);

    List<DiaryUpload> findTop100ByStateAndCreatedAtBeforeOrderByCreatedAtAsc(
            UploadState state,
            LocalDateTime cutoff
    );
}
