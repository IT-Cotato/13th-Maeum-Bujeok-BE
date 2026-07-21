package com.maumbujeok.backend.domain.auth.repository;

import com.maumbujeok.backend.domain.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByLoginId(String loginId);
    Optional<RefreshToken> findByToken(String token);
    void deleteByLoginId(String loginId);
    void deleteByToken(String token);
}
