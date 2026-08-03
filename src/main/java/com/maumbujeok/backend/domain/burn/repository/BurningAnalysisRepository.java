package com.maumbujeok.backend.domain.burn.repository;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
public interface BurningAnalysisRepository extends JpaRepository<BurningAnalysis, Long> { Optional<BurningAnalysis> findByBurningId(Long burningId); @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from BurningAnalysis a where a.id = :id") Optional<BurningAnalysis> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id); }
