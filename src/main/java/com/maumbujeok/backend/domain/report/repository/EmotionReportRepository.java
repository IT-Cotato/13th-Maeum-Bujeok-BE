package com.maumbujeok.backend.domain.report.repository;

import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmotionReportRepository extends JpaRepository<EmotionReport, Long> {
    @Override
    @EntityGraph(attributePaths = "member")
    Optional<EmotionReport> findById(Long id);

    Optional<EmotionReport> findByMemberPhoneNumberAndReportTypeAndPeriodStart(
            String memberPhoneNumber,
            EmotionReportType reportType,
            LocalDate periodStart
    );

    long countByMemberPhoneNumberAndReportTypeAndPeriodStart(
            String memberPhoneNumber,
            EmotionReportType reportType,
            LocalDate periodStart
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select report
            from EmotionReport report
            where report.member.phoneNumber = :phoneNumber
              and report.reportType = :reportType
              and report.periodStart = :periodStart
            """)
    Optional<EmotionReport> findByMemberPhoneNumberAndReportTypeAndPeriodStartForUpdate(
            @Param("phoneNumber") String phoneNumber,
            @Param("reportType") EmotionReportType reportType,
            @Param("periodStart") LocalDate periodStart
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select report from EmotionReport report where report.id = :reportId")
    Optional<EmotionReport> findByIdForUpdate(@Param("reportId") Long reportId);

    Optional<EmotionReport> findByIdAndMemberPhoneNumberAndReportType(
            Long id,
            String memberPhoneNumber,
            EmotionReportType reportType
    );

    List<EmotionReport> findAllByMemberPhoneNumberAndReportTypeOrderByPeriodStartDesc(String memberPhoneNumber, EmotionReportType reportType);

    void deleteByMemberPhoneNumber(String memberPhoneNumber);
}
