package com.maumbujeok.backend.domain.report.repository;

import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NextWeekFlowRepository extends JpaRepository<NextWeekFlow, Long> {
    Optional<NextWeekFlow> findByMemberPhoneNumberAndWeekStart(String memberPhoneNumber, LocalDate weekStart);
}
