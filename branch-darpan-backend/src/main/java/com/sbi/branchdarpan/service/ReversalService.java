package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.ReversalCreateRequest;
import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.WorkflowDecisionRequest;
import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.WorkflowRequestResponse;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.ReversalRequest;
import com.sbi.branchdarpan.model.entity.SurveyAttempt;
import com.sbi.branchdarpan.model.enums.ApprovalDecisionStatus;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.model.enums.SurveyAttemptStatus;
import com.sbi.branchdarpan.model.enums.WorkflowRequestStatus;
import com.sbi.branchdarpan.repository.ReversalRequestRepository;
import com.sbi.branchdarpan.repository.SurveyAnswerRepository;
import com.sbi.branchdarpan.repository.SurveyAttemptRepository;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReversalService {

    private final ReversalRequestRepository reversalRequestRepository;
    private final SurveyAttemptRepository surveyAttemptRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public WorkflowRequestResponse create(ReversalCreateRequest request, UserPrincipal principal) {
        SurveyAttempt attempt = surveyAttemptRepository.findById(request.surveyAttemptId())
            .orElseThrow(() -> new ResourceNotFoundException("Survey attempt not found"));

        ReversalRequest reversalRequest = new ReversalRequest();
        reversalRequest.setSurveyAttempt(attempt);
        reversalRequest.setBranchCode(request.branchCode());
        reversalRequest.setReason(request.reason());
        reversalRequest.setInitiatedBy(userRepository.findById(principal.getId()).orElseThrow());

        ReversalRequest saved = reversalRequestRepository.save(reversalRequest);
        auditService.log(AuditRequestType.REVERSAL_REQUEST, saved.getId(), saved.getStatus().name(), principal, null, saved.getReason());
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkflowRequestResponse> list() {
        return reversalRequestRepository.findAll().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public WorkflowRequestResponse get(Long id) {
        return map(reversalRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reversal request not found")));
    }

    public WorkflowRequestResponse approve(Long id, WorkflowDecisionRequest request, UserPrincipal principal) {
        ReversalRequest reversalRequest = reversalRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reversal request not found"));
        Instant now = Instant.now();
        switch (principal.getRole()) {
            case CIRCLE_CHECKER -> {
                reversalRequest.setCircleCheckerStatus(ApprovalDecisionStatus.APPROVED);
                reversalRequest.setCircleCheckerRemarks(request.remarks());
                reversalRequest.setCircleCheckerActedAt(now);
                reversalRequest.setCircleCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
                reversalRequest.setStatus(WorkflowRequestStatus.PENDING_CC_MAKER);
            }
            case CC_MAKER -> {
                reversalRequest.setCcMakerStatus(ApprovalDecisionStatus.APPROVED);
                reversalRequest.setCcMakerRemarks(request.remarks());
                reversalRequest.setCcMakerActedAt(now);
                reversalRequest.setCcMakerActedBy(userRepository.findById(principal.getId()).orElseThrow());
                reversalRequest.setStatus(WorkflowRequestStatus.PENDING_CC_CHECKER);
            }
            case CC_CHECKER -> {
                reversalRequest.setCcCheckerStatus(ApprovalDecisionStatus.APPROVED);
                reversalRequest.setCcCheckerRemarks(request.remarks());
                reversalRequest.setCcCheckerActedAt(now);
                reversalRequest.setCcCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
                reversalRequest.setStatus(WorkflowRequestStatus.APPROVED);
                resetSurveyAttempt(reversalRequest.getSurveyAttempt());
            }
            default -> throw new IllegalArgumentException("Role not allowed to approve reversal");
        }
        auditService.log(AuditRequestType.REVERSAL_REQUEST, reversalRequest.getId(), reversalRequest.getStatus().name(), principal, null, request.remarks());
        return map(reversalRequest);
    }

    public WorkflowRequestResponse reject(Long id, WorkflowDecisionRequest request, UserPrincipal principal) {
        ReversalRequest reversalRequest = reversalRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reversal request not found"));
        Instant now = Instant.now();
        if (principal.getRole() == Role.CIRCLE_CHECKER) {
            reversalRequest.setCircleCheckerStatus(ApprovalDecisionStatus.REJECTED);
            reversalRequest.setCircleCheckerRemarks(request.remarks());
            reversalRequest.setCircleCheckerActedAt(now);
            reversalRequest.setCircleCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
        } else if (principal.getRole() == Role.CC_MAKER) {
            reversalRequest.setCcMakerStatus(ApprovalDecisionStatus.REJECTED);
            reversalRequest.setCcMakerRemarks(request.remarks());
            reversalRequest.setCcMakerActedAt(now);
            reversalRequest.setCcMakerActedBy(userRepository.findById(principal.getId()).orElseThrow());
        } else if (principal.getRole() == Role.CC_CHECKER) {
            reversalRequest.setCcCheckerStatus(ApprovalDecisionStatus.REJECTED);
            reversalRequest.setCcCheckerRemarks(request.remarks());
            reversalRequest.setCcCheckerActedAt(now);
            reversalRequest.setCcCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
        }
        reversalRequest.setStatus(WorkflowRequestStatus.REJECTED);
        auditService.log(AuditRequestType.REVERSAL_REQUEST, reversalRequest.getId(), reversalRequest.getStatus().name(), principal, null, request.remarks());
        return map(reversalRequest);
    }

    private void resetSurveyAttempt(SurveyAttempt attempt) {
        surveyAnswerRepository.deleteByAttemptId(attempt.getId());
        attempt.setStatus(SurveyAttemptStatus.DRAFT);
        attempt.setAttemptNumber(attempt.getAttemptNumber() + 1);
        attempt.setSubmittedAt(null);
    }

    private WorkflowRequestResponse map(ReversalRequest request) {
        return new WorkflowRequestResponse(
            request.getId(),
            request.getSurveyAttempt().getId(),
            "REVERSAL",
            request.getBranchCode(),
            request.getSurveyAttempt().getBranchName(),
            request.getReason(),
            request.getStatus().name(),
            request.getCreatedAt().toString()
        );
    }
}
