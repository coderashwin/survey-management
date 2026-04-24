package com.sbi.branchdarpan.model.dto.survey;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class SurveyDtos {

    private SurveyDtos() {
    }

    public record SurveySummary(
        Long id,
        String title,
        String frequency,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive
    ) {
    }

    public record QuestionOptionResponse(Long id, String optionText, String optionValue, int displayOrder) {
    }

    public record QuestionResponse(
        Long id,
        String questionText,
        String optionType,
        BigDecimal weightage,
        String frequency,
        int displayOrder,
        Long dependsOnQuestionId,
        String dependsOnAnswer,
        List<QuestionOptionResponse> options
    ) {
    }

    public record SubsectionResponse(Long id, String name, int displayOrder, List<QuestionResponse> questions) {
    }

    public record SectionResponse(
        Long id,
        String name,
        int displayOrder,
        Long isMutuallyExclusiveWith,
        List<SubsectionResponse> subsections
    ) {
    }

    public record SurveyResponse(
        Long id,
        String title,
        String frequency,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive,
        List<SectionResponse> sections
    ) {
    }

    public record SurveyEndDateRequest(@NotNull LocalDate endDate) {
    }

    public record CreateAttemptRequest(@NotNull Long surveyId) {
    }

    public record AnswerInput(Long questionId, String answerValue, String filePath) {
    }

    public record UpdateAttemptRequest(
        @NotBlank String action,
        @NotEmpty List<AnswerInput> answers
    ) {
    }

    public record AnswerResponse(
        Long questionId,
        String answerValue,
        String filePath,
        String branchCheckerStatus,
        String branchCheckerRemarks,
        String rboCheckerStatus,
        String rboCheckerRemarks,
        boolean isLocked
    ) {
    }

    public record SurveyAttemptResponse(
        Long id,
        Long surveyId,
        String surveyTitle,
        String branchCode,
        String branchName,
        String status,
        int attemptNumber,
        String submittedAt,
        List<AnswerResponse> answers
    ) {
    }

    public record DraftRequest(@NotNull Long surveyId, @NotNull Map<String, Object> draftData) {
    }

    public record DraftResponse(Long surveyId, Map<String, Object> draftData) {
    }

    public record QuestionDecisionRequest(@NotBlank String status, String remarks) {
    }

    public record ApprovalSubmitResponse(String message, String newStatus) {
    }
}
