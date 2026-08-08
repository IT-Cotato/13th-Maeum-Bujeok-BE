package com.maumbujeok.backend.domain.report.repository;

import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionReportRepository extends JpaRepository<EmotionReport, Long> {
    @Override
    @EntityGraph(attributePaths = "member")
    Optional<EmotionReport> findById(Long id);

    Optional<EmotionReport> findByMemberPhoneNumberAndReportTypeAndPeriodStart(
            String memberPhoneNumber,
            EmotionReportType reportType,
            LocalDate periodStart
    );

    Optional<EmotionReport> findByIdAndMemberPhoneNumberAndReportType(
            Long id,
            String memberPhoneNumber,
            EmotionReportType reportType
    );

    List<EmotionReport> findAllByMemberPhoneNumberAndReportTypeOrderByPeriodStartDesc(String memberPhoneNumber, EmotionReportType reportType);

    void deleteByMemberPhoneNumber(String memberPhoneNumber);
}
