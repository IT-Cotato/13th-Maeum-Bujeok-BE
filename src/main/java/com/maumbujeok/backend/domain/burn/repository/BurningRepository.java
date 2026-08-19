package com.maumbujeok.backend.domain.burn.repository;

import com.maumbujeok.backend.domain.burn.domain.Burning;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BurningRepository extends JpaRepository<Burning, Long> {
    Optional<Burning> findByIdAndMemberPhoneNumber(Long id, String phoneNumber);
    boolean existsByMemberPhoneNumberAndDiaryId(String phoneNumber, Long diaryId);
    void deleteByMemberPhoneNumber(String phoneNumber);

    @Query("select b from Burning b where b.member.phoneNumber=:phoneNumber and b.burnedAt >= :from and b.burnedAt < :to order by b.burnedAt desc, b.id desc")
    List<Burning> findAllByMemberPhoneNumberAndBurnedAtBetween(@Param("phoneNumber") String phoneNumber, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select b from Burning b
            left join com.maumbujeok.backend.domain.diary.domain.Diary d on d.id = b.diaryId
            where b.member.phoneNumber = :phoneNumber
              and (
                (b.sourceType = com.maumbujeok.backend.domain.burn.domain.BurningSourceType.DIARY
                 and d.recordedDate >= :periodStart and d.recordedDate <= :periodEnd)
                or
                (b.sourceType = com.maumbujeok.backend.domain.burn.domain.BurningSourceType.DIRECT
                 and b.burnedAt >= :from and b.burnedAt < :to)
              )
            order by b.burnedAt desc, b.id desc
            """)
    List<Burning> findAllForWeeklyReport(
            @Param("phoneNumber") String phoneNumber,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("select b from Burning b where b.member.phoneNumber=:phoneNumber and (:cursor is null or b.id < :cursor) order by b.burnedAt desc, b.id desc")
    List<Burning> findCursor(@Param("phoneNumber") String phoneNumber, @Param("cursor") Long cursor, Pageable pageable);
}
