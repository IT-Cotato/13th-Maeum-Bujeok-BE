package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryAnalysisRepository extends JpaRepository<DiaryAnalysis, Long> {
    @Override
    @EntityGraph(attributePaths = "diary")
    Optional<DiaryAnalysis> findById(Long id);

    Optional<DiaryAnalysis> findByDiaryIdAndDiaryMemberId(Long diaryId, Long memberId);
}
