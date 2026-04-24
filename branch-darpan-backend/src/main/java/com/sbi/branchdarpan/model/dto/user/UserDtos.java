package com.sbi.branchdarpan.model.dto.user;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class UserDtos {

    private UserDtos() {
    }

    public record HrmsUserResponse(
        String pfid,
        String name,
        String email,
        String mobile,
        String designation,
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

    public record UserSummary(
        Long id,
        String pfid,
        String name,
        String email,
        String mobile,
        String designation,
        String role,
        String circleCode,
        String circleName,
        String aoCode,
        String aoName,
        String rboCode,
        String rboName,
        String branchCode,
        String branchName,
        boolean isActive
    ) {
    }

    public record UserRequestCreateRequest(
        @NotBlank String pfid,
        @NotBlank String requestedRole,
        @NotBlank String name,
        String email,
        String mobile,
        String designation,
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

    public record UserRequestSummary(
        Long id,
        String pfid,
        String requestedRole,
        String name,
        String status,
        String currentApproverRole,
        String createdAt,
        String remarks
    ) {
    }

    public record ApprovalRequest(String remarks) {
    }

    public record AllowedRolesResponse(List<String> roles) {
    }
}
