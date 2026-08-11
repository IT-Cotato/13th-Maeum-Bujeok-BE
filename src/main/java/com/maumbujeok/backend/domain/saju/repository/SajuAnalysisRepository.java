package com.maumbujeok.backend.domain.saju.repository;

import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SajuAnalysisRepository extends JpaRepository<SajuAnalysis, Long> {

    Optional<SajuAnalysis> findByIdAndMemberPhoneNumber(Long id, String phoneNumber);

    List<SajuAnalysis> findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(String phoneNumber);

    void deleteByMemberPhoneNumber(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select analysis
            from SajuAnalysis analysis
            where analysis.member.phoneNumber = :phoneNumber
            order by analysis.createdAt desc, analysis.id desc
            """)
    List<SajuAnalysis> findAllByMemberPhoneNumberForUpdate(@Param("phoneNumber") String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select analysis
            from SajuAnalysis analysis
            join fetch analysis.member
            where analysis.id = :analysisId
            """)
    Optional<SajuAnalysis> findByIdForUpdate(@Param("analysisId") Long analysisId);
}
