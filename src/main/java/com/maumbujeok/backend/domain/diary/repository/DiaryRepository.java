package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByIdAndMemberId(Long id, Long memberId);
}
