package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.ExemptionCreateRequest;
import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.WorkflowDecisionRequest;
import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.WorkflowRequestResponse;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.security.UserPrincipal;
import com.sbi.branchdarpan.service.ExemptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/exemptions")
@RequiredArgsConstructor
public class ExemptionController {

    private final ExemptionService exemptionService;

    @PostMapping
    public WorkflowRequestResponse create(
        @Valid @RequestBody ExemptionCreateRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return exemptionService.create(request, principal);
    }

    @GetMapping
    public List<WorkflowRequestResponse> list() {
        return exemptionService.list();
    }

    @GetMapping("/{id}")
    public WorkflowRequestResponse get(@PathVariable Long id) {
        return exemptionService.get(id);
    }

    @PutMapping("/{id}/approve")
    public WorkflowRequestResponse approve(
        @PathVariable Long id,
        @RequestBody WorkflowDecisionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return exemptionService.approve(id, request, principal);
    }

    @PutMapping("/{id}/reject")
    public WorkflowRequestResponse reject(
        @PathVariable Long id,
        @RequestBody WorkflowDecisionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return exemptionService.reject(id, request, principal);
    }
}
