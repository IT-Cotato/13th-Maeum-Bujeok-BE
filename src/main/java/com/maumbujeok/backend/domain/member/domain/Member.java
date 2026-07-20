package com.maumbujeok.backend.domain.member.domain;

import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "birth_date", length = 8)
    private String birthDate;

    @Column(name = "terms_agreed_at")
    private LocalDateTime termsAgreedAt;

    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    @Column(name = "sensitive_data_agreed_at")
    private LocalDateTime sensitiveDataAgreedAt;

    @Column(name = "marketing_agreed_at")
    private LocalDateTime marketingAgreedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public Member(String loginId, String phoneNumber, String passwordHash, String birthDate,
                  LocalDateTime termsAgreedAt, LocalDateTime privacyAgreedAt,
                  LocalDateTime sensitiveDataAgreedAt, LocalDateTime marketingAgreedAt, Role role) {
        this.loginId = loginId;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.birthDate = birthDate;
        this.termsAgreedAt = termsAgreedAt;
        this.privacyAgreedAt = privacyAgreedAt;
        this.sensitiveDataAgreedAt = sensitiveDataAgreedAt;
        this.marketingAgreedAt = marketingAgreedAt;
        this.role = role != null ? role : Role.ROLE_USER;
    }

    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }
}
