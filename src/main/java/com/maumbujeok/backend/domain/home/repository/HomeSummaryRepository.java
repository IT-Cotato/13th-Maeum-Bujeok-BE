package com.maumbujeok.backend.domain.home.repository;

import com.maumbujeok.backend.domain.home.domain.HomeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HomeSummaryRepository extends JpaRepository<HomeSummary, Long> {
    Optional<HomeSummary> findByMemberPhoneNumberAndSummaryDate(String phoneNumber, LocalDate summaryDate);
    boolean existsByMemberPhoneNumberAndSummaryDate(String phoneNumber, LocalDate summaryDate);
    void deleteByMemberPhoneNumber(String phoneNumber);
}
