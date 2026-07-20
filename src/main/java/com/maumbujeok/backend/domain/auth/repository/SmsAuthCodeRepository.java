package com.maumbujeok.backend.domain.auth.repository;

import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsAuthCodeRepository extends JpaRepository<SmsAuthCode, Long> {
    Optional<SmsAuthCode> findByPhoneNumberAndCode(String phoneNumber, String code);
    Optional<SmsAuthCode> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
