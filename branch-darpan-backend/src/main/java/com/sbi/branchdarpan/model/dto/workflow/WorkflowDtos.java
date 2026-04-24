package com.sbi.branchdarpan.model.dto.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class WorkflowDtos {

    private WorkflowDtos() {
    }

    public record ReversalCreateRequest(
        @NotNull Long surveyAttemptId,
        @NotBlank String branchCode,
        @NotBlank String reason
    ) {
    }

    public record ExemptionCreateRequest(
        @NotNull Long surveyId,
        @NotBlank String branchCode,
        String branchName,
        @NotBlank String reason
    ) {
    }

    public record WorkflowDecisionRequest(String remarks) {
    }

    public record WorkflowRequestResponse(
        Long id,
        Long referenceId,
        String type,
        String branchCode,
        String branchName,
        String reason,
        String status,
        String createdAt
    ) {
    }
}
