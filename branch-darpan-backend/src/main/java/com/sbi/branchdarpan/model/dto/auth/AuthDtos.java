package com.sbi.branchdarpan.model.dto.auth;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SsoLoginRequest(@NotBlank String ssoToken) {
    }

    public record UserProfile(
        Long id,
        String pfid,
        String name,
        String role,
        String circleCode,
        String circleName,
        String aoCode,
        String aoName,
        String rboCode,
        String rboName,
        String branchCode,
        String branchName
    ) {
    }

    public record AuthResponse(String jwt, UserProfile user) {
    }

    public record MessageResponse(String message) {
    }
}
