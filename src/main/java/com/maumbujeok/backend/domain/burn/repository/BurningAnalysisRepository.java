package com.maumbujeok.backend.domain.burn.repository;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BurningAnalysisRepository extends JpaRepository<BurningAnalysis, Long> { Optional<BurningAnalysis> findByBurningId(Long burningId); }
