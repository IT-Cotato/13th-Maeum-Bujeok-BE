package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.domain.diary.domain.Diary;
<<<<<<< HEAD
import java.time.LocalDate;
=======
import java.time.LocalDateTime;
>>>>>>> e979575fd410689772266a20957ea06b3cff6477
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Optional<Diary> findByIdAndMemberPhoneNumber(Long id, String phoneNumber);
    boolean existsByMemberPhoneNumberAndRecordedDate(String phoneNumber, LocalDate recordedDate);

<<<<<<< HEAD
    List<Diary> findAllByMemberPhoneNumberOrderByRecordedDateDescCreatedAtDescIdDesc(String phoneNumber);
    List<Diary> findAllByMemberPhoneNumberAndRecordedDateOrderByCreatedAtDescIdDesc(
            String phoneNumber, LocalDate recordedDate);
    List<Diary> findAllByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThanOrderByRecordedDateDescCreatedAtDescIdDesc(
            String phoneNumber, LocalDate fromInclusive, LocalDate toExclusive);

    @Query("""
            select diary from Diary diary
            where diary.member.phoneNumber = :phoneNumber
              and (:cursorDate is null
                   or diary.recordedDate < :cursorDate
                   or (diary.recordedDate = :cursorDate and diary.id < :cursorId))
            order by diary.recordedDate desc, diary.id desc
            """)
    List<Diary> findCursor(
            @Param("phoneNumber") String phoneNumber,
            @Param("cursorDate") LocalDate cursorDate,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
=======
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
>>>>>>> e979575fd410689772266a20957ea06b3cff6477
