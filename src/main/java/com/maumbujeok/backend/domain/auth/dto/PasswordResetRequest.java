package com.maumbujeok.backend.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequest {
    private String phoneNumber;
    private String newPassword;
}
