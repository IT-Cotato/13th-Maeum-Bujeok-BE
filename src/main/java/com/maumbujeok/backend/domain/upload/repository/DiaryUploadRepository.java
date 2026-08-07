package com.maumbujeok.backend.domain.upload.repository;

import com.maumbujeok.backend.domain.upload.domain.DiaryUpload;
import com.maumbujeok.backend.domain.upload.domain.UploadState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryUploadRepository extends JpaRepository<DiaryUpload, UUID> {
    Optional<DiaryUpload> findByIdAndMemberPhoneNumber(UUID id, String phoneNumber);

    @Modifying
    @Query("delete from DiaryUpload upload where upload.member.phoneNumber = :phoneNumber")
    void deleteByMemberPhoneNumber(@Param("phoneNumber") String phoneNumber);

    List<DiaryUpload> findAllByIdInAndMemberPhoneNumber(List<UUID> ids, String phoneNumber);

    List<DiaryUpload> findAllByDiaryIdAndStateOrderBySortOrderAsc(Long diaryId, UploadState state);

    List<DiaryUpload> findTop100ByStateOrderByCreatedAtAsc(UploadState state);

    List<DiaryUpload> findTop100ByStateAndCreatedAtBeforeOrderByCreatedAtAsc(
            UploadState state,
            LocalDateTime cutoff
    );
}
