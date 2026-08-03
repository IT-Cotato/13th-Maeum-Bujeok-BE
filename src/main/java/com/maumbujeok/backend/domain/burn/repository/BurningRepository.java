package com.maumbujeok.backend.domain.burn.repository;
import com.maumbujeok.backend.domain.burn.domain.Burning;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface BurningRepository extends JpaRepository<Burning, Long> {
 Optional<Burning> findByIdAndMemberPhoneNumber(Long id, String phoneNumber);
 boolean existsByMemberPhoneNumberAndDiaryId(String phoneNumber, Long diaryId);
 @Query("select b from Burning b where b.member.phoneNumber=:phoneNumber and (:cursor is null or b.id < :cursor) order by b.burnedAt desc, b.id desc")
 List<Burning> findCursor(@Param("phoneNumber") String phoneNumber, @Param("cursor") Long cursor, Pageable pageable);
}
