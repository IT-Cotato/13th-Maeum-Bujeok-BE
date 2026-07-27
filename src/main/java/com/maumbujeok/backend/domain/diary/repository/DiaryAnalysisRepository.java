package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryAnalysisRepository extends JpaRepository<DiaryAnalysis, Long> {
    @Override
    @EntityGraph(attributePaths = "diary")
    Optional<DiaryAnalysis> findById(Long id);

    Optional<DiaryAnalysis> findByDiaryIdAndDiaryMemberPhoneNumber(Long diaryId, String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select analysis from DiaryAnalysis analysis join fetch analysis.diary where analysis.id = :analysisId")
    Optional<DiaryAnalysis> findByIdForUpdate(@Param("analysisId") Long analysisId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select analysis
            from DiaryAnalysis analysis
            join fetch analysis.diary diary
            where diary.id = :diaryId
              and diary.member.phoneNumber = :phoneNumber
            """)
    Optional<DiaryAnalysis> findOwnedByDiaryIdForUpdate(
            @Param("diaryId") Long diaryId,
            @Param("phoneNumber") String phoneNumber
    );

    @Query("""
            select analysis.reportEmotion as emotion, count(analysis) as count
            from DiaryAnalysis analysis
            where analysis.diary.member.phoneNumber = :phoneNumber
              and analysis.diary.recordedDate between :from and :to
              and analysis.status in :statuses
              and analysis.reportEmotion is not null
            group by analysis.reportEmotion
            """)
    List<DiaryEmotionCountProjection> countReportEmotions(
            @Param("phoneNumber") String phoneNumber,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("statuses") Collection<DiaryAnalysisStatus> statuses
    );

    void deleteByDiaryIn(List<Diary> diaries);
}
