package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.ApprovalSubmitResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.QuestionDecisionRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyAttemptResponse;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.exception.BadRequestException;
import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.SurveyAnswer;
import com.sbi.branchdarpan.model.entity.SurveyAttempt;
import com.sbi.branchdarpan.model.enums.ApprovalDecisionStatus;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.model.enums.SurveyAttemptStatus;
import com.sbi.branchdarpan.repository.SurveyAnswerRepository;
import com.sbi.branchdarpan.repository.SurveyAttemptRepository;
import com.sbi.branchdarpan.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ApprovalService {

    private final SurveyAttemptRepository surveyAttemptRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final AuditService auditService;
    private final SurveyAttemptService surveyAttemptService;

    @Transactional(readOnly = true)
    public List<SurveyAttemptResponse> getPendingApprovals(UserPrincipal principal) {
        List<SurveyAttemptStatus> statuses = switch (principal.getRole()) {
            case BRANCH_CHECKER -> List.of(SurveyAttemptStatus.PENDING_BRANCH_CHECKER);
            case RBO_CHECKER -> List.of(SurveyAttemptStatus.PENDING_RBO_CHECKER);
            default -> List.of();
        };
        return surveyAttemptRepository.findByStatusInOrderByUpdatedAtDesc(statuses)
            .stream()
            .map(surveyAttemptService::toResponse)
            .toList();
    }

    public SurveyAttemptResponse approveQuestion(Long attemptId, Long questionId, QuestionDecisionRequest request, UserPrincipal principal) {
        SurveyAnswer answer = surveyAnswerRepository.findByAttemptIdAndQuestionId(attemptId, questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Survey answer not found"));

        ApprovalDecisionStatus decision = ApprovalDecisionStatus.valueOf(request.status().toUpperCase());
        if (decision == ApprovalDecisionStatus.REJECTED && (request.remarks() == null || request.remarks().isBlank())) {
            throw new BadRequestException("Remarks are mandatory when rejecting a question");
        }

        if (principal.getRole() == Role.BRANCH_CHECKER) {
            answer.setBranchCheckerStatus(decision);
            answer.setBranchCheckerRemarks(request.remarks());
            answer.getAttempt().setBranchCheckerActedAt(Instant.now());
        } else if (principal.getRole() == Role.RBO_CHECKER) {
            answer.setRboCheckerStatus(decision);
            answer.setRboCheckerRemarks(request.remarks());
            answer.getAttempt().setRboCheckerActedAt(Instant.now());
            if (decision == ApprovalDecisionStatus.APPROVED) {
                answer.setLocked(true);
            }
        } else {
            throw new BadRequestException("Only branch and RBO checkers can review answers");
        }

        return surveyAttemptService.toResponse(answer.getAttempt());
    }

    public ApprovalSubmitResponse submitAllDecisions(Long attemptId, UserPrincipal principal) {
        SurveyAttempt attempt = surveyAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResourceNotFoundException("Survey attempt not found"));
        List<SurveyAnswer> answers = surveyAnswerRepository.findByAttemptId(attemptId);

        String message;
        if (principal.getRole() == Role.BRANCH_CHECKER) {
            boolean anyRejected = answers.stream().anyMatch(answer -> answer.getBranchCheckerStatus() == ApprovalDecisionStatus.REJECTED);
            if (anyRejected) {
                attempt.setStatus(SurveyAttemptStatus.REJECTED_BY_BRANCH_CHECKER);
                message = "Survey rejected, sent back to Branch Maker";
            } else {
                attempt.setStatus(SurveyAttemptStatus.PENDING_RBO_CHECKER);
                message = "Survey forwarded to RBO Checker";
            }
        } else if (principal.getRole() == Role.RBO_CHECKER) {
            boolean anyRejected = answers.stream().anyMatch(answer -> answer.getRboCheckerStatus() == ApprovalDecisionStatus.REJECTED);
            if (anyRejected) {
                attempt.setStatus(SurveyAttemptStatus.REJECTED_BY_RBO_CHECKER);
                message = "Survey rejected, sent back to Branch Maker";
            } else {
                attempt.setStatus(SurveyAttemptStatus.APPROVED);
                answers.stream()
                    .filter(answer -> answer.getRboCheckerStatus() == ApprovalDecisionStatus.APPROVED)
                    .forEach(answer -> answer.setLocked(true));
                message = "Survey fully approved";
            }
        } else {
            throw new BadRequestException("Only branch and RBO checkers can submit decisions");
        }

        auditService.log(AuditRequestType.SURVEY_ATTEMPT, attempt.getId(), attempt.getStatus().name(), principal, null, message);
        return new ApprovalSubmitResponse(message, attempt.getStatus().name());
    }
}
