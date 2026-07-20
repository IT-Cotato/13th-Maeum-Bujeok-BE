package com.maumbujeok.backend.domain.auth.domain;

import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_auth_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsAuthCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_auth_code_id")
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Builder
    public SmsAuthCode(String phoneNumber, String code, LocalDateTime expiredAt) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.expiredAt = expiredAt;
        this.isVerified = false;
    }

    public void verify() {
        this.isVerified = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}
