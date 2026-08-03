package com.maumbujeok.backend.domain.talisman.repository;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalismanRepository extends JpaRepository<Talisman, Long>, TalismanRepositoryCustom {
    List<Talisman> findAllByMemberPhoneNumberOrderByCreatedAtDesc(String memberPhoneNumber);
    void deleteByMemberPhoneNumber(String memberPhoneNumber);
    boolean existsByBurnRitualId(Long burnRitualId);
    Optional<Talisman> findByBurnRitualIdAndMemberPhoneNumber(Long burnRitualId, String memberPhoneNumber);
}


