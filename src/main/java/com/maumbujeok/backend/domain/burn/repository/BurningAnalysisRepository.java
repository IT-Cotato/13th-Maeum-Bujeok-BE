package com.maumbujeok.backend.domain.burn.repository;

import com.maumbujeok.backend.domain.burn.domain.BurningAnalysis;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BurningAnalysisRepository extends JpaRepository<BurningAnalysis, Long> {

    Optional<BurningAnalysis> findByBurningId(Long burningId);

    @Modifying
    @Query(
            value = """
                    delete from burning_analyses
                    where burning_id in (
                        select id
                        from burnings
                        where member_phone_number = :phoneNumber
                    )
                    """,
            nativeQuery = true
    )
    void deleteByBurningMemberPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BurningAnalysis a where a.burning.id = :burningId")
    Optional<BurningAnalysis> findByBurningIdForUpdate(@Param("burningId") Long burningId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BurningAnalysis a where a.id = :id")
    Optional<BurningAnalysis> findByIdForUpdate(@Param("id") Long id);
}
