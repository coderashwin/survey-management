package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.ApprovalSubmitResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.QuestionDecisionRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyAttemptResponse;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.security.UserPrincipal;
import com.sbi.branchdarpan.service.ApprovalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/surveys/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    public List<SurveyAttemptResponse> getPendingApprovals(@AuthenticationPrincipal UserPrincipal principal) {
        return approvalService.getPendingApprovals(principal);
    }

    @PutMapping("/{attemptId}/question/{questionId}")
    public SurveyAttemptResponse approveQuestion(
        @PathVariable Long attemptId,
        @PathVariable Long questionId,
        @Valid @RequestBody QuestionDecisionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return approvalService.approveQuestion(attemptId, questionId, request, principal);
    }

    @PutMapping("/{attemptId}/submit")
    public ApprovalSubmitResponse submit(
        @PathVariable Long attemptId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return approvalService.submitAllDecisions(attemptId, principal);
    }
}
