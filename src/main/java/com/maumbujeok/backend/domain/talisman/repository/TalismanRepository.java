package com.maumbujeok.backend.domain.talisman.repository;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalismanRepository extends JpaRepository<Talisman, Long>, TalismanRepositoryCustom {
    List<Talisman> findAllByMemberPhoneNumberOrderByCreatedAtDesc(String memberPhoneNumber);
    void deleteByMemberPhoneNumber(String memberPhoneNumber);
}
