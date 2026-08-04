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
    @Column(name = "phone_number", nullable = false, length = 50, unique = true)
    private String phoneNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "birth_date", length = 20)
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

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
    public Member(String name, String email, String phoneNumber, String passwordHash, String birthDate,
                  Provider provider, String providerId,
                  LocalDateTime termsAgreedAt, LocalDateTime privacyAgreedAt,
                  LocalDateTime sensitiveDataAgreedAt, LocalDateTime marketingAgreedAt, Role role) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.birthDate = birthDate;
        this.provider = provider != null ? provider : Provider.LOCAL;
        this.providerId = providerId;
        this.termsAgreedAt = termsAgreedAt;
        this.privacyAgreedAt = privacyAgreedAt;
        this.sensitiveDataAgreedAt = sensitiveDataAgreedAt;
        this.marketingAgreedAt = marketingAgreedAt;
        this.role = role != null ? role : Role.ROLE_USER;
    }

    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public enum Provider {
        LOCAL, GOOGLE
    }

    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }
}
