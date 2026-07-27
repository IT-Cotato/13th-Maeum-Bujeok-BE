package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByIdAndMemberPhoneNumber(Long id, String phoneNumber);

    List<Diary> findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(String phoneNumber);

    boolean existsByMemberPhoneNumberAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime endedAtExclusive
    );

    List<Diary> findAllByMemberPhoneNumberAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAscIdAsc(
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime endedAtExclusive
    );
}
