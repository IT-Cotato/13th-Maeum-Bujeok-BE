package com.maumbujeok.backend.domain.report.repository;

import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NextWeekFlowRepository extends JpaRepository<NextWeekFlow, Long> {
    Optional<NextWeekFlow> findByMemberPhoneNumberAndWeekStart(String memberPhoneNumber, LocalDate weekStart);
    void deleteByMemberPhoneNumber(String memberPhoneNumber);

    @Modifying
    @Query("UPDATE NextWeekFlow f SET f.updatedAt = :now WHERE f.id = :flowId AND f.generationStatus = :status AND f.adviceText IS NULL AND (f.updatedAt <= :cutoff OR f.updatedAt IS NULL)")
    int tryAcquireStaleRecovery(
            @Param("flowId") Long flowId,
            @Param("status") NextWeekFlowGenerationStatus status,
            @Param("cutoff") LocalDateTime cutoff,
            @Param("now") LocalDateTime now
    );
}
