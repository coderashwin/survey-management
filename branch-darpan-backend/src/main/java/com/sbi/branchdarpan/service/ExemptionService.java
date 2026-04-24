package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.ExemptionCreateRequest;
import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.WorkflowDecisionRequest;
import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.WorkflowRequestResponse;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.ExemptionRequest;
import com.sbi.branchdarpan.model.enums.ApprovalDecisionStatus;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.model.enums.WorkflowRequestStatus;
import com.sbi.branchdarpan.repository.ExemptionRequestRepository;
import com.sbi.branchdarpan.repository.SurveyRepository;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ExemptionService {

    private final ExemptionRequestRepository exemptionRequestRepository;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public WorkflowRequestResponse create(ExemptionCreateRequest request, UserPrincipal principal) {
        ExemptionRequest exemptionRequest = new ExemptionRequest();
        exemptionRequest.setSurvey(surveyRepository.findById(request.surveyId()).orElseThrow(() -> new ResourceNotFoundException("Survey not found")));
        exemptionRequest.setBranchCode(request.branchCode());
        exemptionRequest.setBranchName(request.branchName());
        exemptionRequest.setReason(request.reason());
        exemptionRequest.setInitiatedBy(userRepository.findById(principal.getId()).orElseThrow());

        ExemptionRequest saved = exemptionRequestRepository.save(exemptionRequest);
        auditService.log(AuditRequestType.EXEMPTION_REQUEST, saved.getId(), saved.getStatus().name(), principal, null, saved.getReason());
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkflowRequestResponse> list() {
        return exemptionRequestRepository.findAll().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public WorkflowRequestResponse get(Long id) {
        return map(exemptionRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exemption request not found")));
    }

    public WorkflowRequestResponse approve(Long id, WorkflowDecisionRequest request, UserPrincipal principal) {
        ExemptionRequest exemptionRequest = exemptionRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exemption request not found"));
        Instant now = Instant.now();
        switch (principal.getRole()) {
            case CIRCLE_CHECKER -> {
                exemptionRequest.setCircleCheckerStatus(ApprovalDecisionStatus.APPROVED);
                exemptionRequest.setCircleCheckerRemarks(request.remarks());
                exemptionRequest.setCircleCheckerActedAt(now);
                exemptionRequest.setCircleCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
                exemptionRequest.setStatus(WorkflowRequestStatus.PENDING_CC_MAKER);
            }
            case CC_MAKER -> {
                exemptionRequest.setCcMakerStatus(ApprovalDecisionStatus.APPROVED);
                exemptionRequest.setCcMakerRemarks(request.remarks());
                exemptionRequest.setCcMakerActedAt(now);
                exemptionRequest.setCcMakerActedBy(userRepository.findById(principal.getId()).orElseThrow());
                exemptionRequest.setStatus(WorkflowRequestStatus.PENDING_CC_CHECKER);
            }
            case CC_CHECKER -> {
                exemptionRequest.setCcCheckerStatus(ApprovalDecisionStatus.APPROVED);
                exemptionRequest.setCcCheckerRemarks(request.remarks());
                exemptionRequest.setCcCheckerActedAt(now);
                exemptionRequest.setCcCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
                exemptionRequest.setStatus(WorkflowRequestStatus.APPROVED);
            }
            default -> throw new IllegalArgumentException("Role not allowed to approve exemption");
        }
        auditService.log(AuditRequestType.EXEMPTION_REQUEST, exemptionRequest.getId(), exemptionRequest.getStatus().name(), principal, null, request.remarks());
        return map(exemptionRequest);
    }

    public WorkflowRequestResponse reject(Long id, WorkflowDecisionRequest request, UserPrincipal principal) {
        ExemptionRequest exemptionRequest = exemptionRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exemption request not found"));
        Instant now = Instant.now();
        if (principal.getRole() == Role.CIRCLE_CHECKER) {
            exemptionRequest.setCircleCheckerStatus(ApprovalDecisionStatus.REJECTED);
            exemptionRequest.setCircleCheckerRemarks(request.remarks());
            exemptionRequest.setCircleCheckerActedAt(now);
            exemptionRequest.setCircleCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
        } else if (principal.getRole() == Role.CC_MAKER) {
            exemptionRequest.setCcMakerStatus(ApprovalDecisionStatus.REJECTED);
            exemptionRequest.setCcMakerRemarks(request.remarks());
            exemptionRequest.setCcMakerActedAt(now);
            exemptionRequest.setCcMakerActedBy(userRepository.findById(principal.getId()).orElseThrow());
        } else if (principal.getRole() == Role.CC_CHECKER) {
            exemptionRequest.setCcCheckerStatus(ApprovalDecisionStatus.REJECTED);
            exemptionRequest.setCcCheckerRemarks(request.remarks());
            exemptionRequest.setCcCheckerActedAt(now);
            exemptionRequest.setCcCheckerActedBy(userRepository.findById(principal.getId()).orElseThrow());
        }
        exemptionRequest.setStatus(WorkflowRequestStatus.REJECTED);
        auditService.log(AuditRequestType.EXEMPTION_REQUEST, exemptionRequest.getId(), exemptionRequest.getStatus().name(), principal, null, request.remarks());
        return map(exemptionRequest);
    }

    private WorkflowRequestResponse map(ExemptionRequest request) {
        return new WorkflowRequestResponse(
            request.getId(),
            request.getSurvey().getId(),
            "EXEMPTION",
            request.getBranchCode(),
            request.getBranchName(),
            request.getReason(),
            request.getStatus().name(),
            request.getCreatedAt().toString()
        );
    }
}
