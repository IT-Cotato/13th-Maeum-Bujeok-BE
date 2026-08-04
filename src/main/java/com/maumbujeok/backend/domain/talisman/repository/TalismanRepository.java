package com.maumbujeok.backend.domain.talisman.repository;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalismanRepository extends JpaRepository<Talisman, Long>, TalismanRepositoryCustom {
    List<Talisman> findAllByMemberPhoneNumberOrderByCreatedAtDesc(String memberPhoneNumber);
    List<Talisman> findAllByMemberPhoneNumberAndRecordedAtBetweenOrderByRecordedAtDescCreatedAtDesc(String phoneNumber, java.time.LocalDate from, java.time.LocalDate to);
    void deleteByMemberPhoneNumber(String memberPhoneNumber);
    boolean existsByBurnRitualId(Long burnRitualId);
    Optional<Talisman> findByBurnRitualIdAndMemberPhoneNumber(Long burnRitualId, String memberPhoneNumber);
    Optional<Talisman> findByIdAndMemberPhoneNumber(Long id, String memberPhoneNumber);
}
