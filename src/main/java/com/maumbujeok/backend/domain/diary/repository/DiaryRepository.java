package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByIdAndMemberPhoneNumber(Long id, String phoneNumber);

    List<Diary> findAllByMemberPhoneNumberOrderByRecordedDateDescCreatedAtDescIdDesc(String phoneNumber);

    List<Diary> findAllByMemberPhoneNumberAndRecordedDateOrderByCreatedAtDescIdDesc(
            String phoneNumber,
            LocalDate recordedDate
    );

    List<Diary> findAllByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThanOrderByRecordedDateDescCreatedAtDescIdDesc(
            String phoneNumber,
            LocalDate fromInclusive,
            LocalDate toExclusive
    );
}
