package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.workflow.WorkflowDtos.ReversalCreateRequest;
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
import com.sbi.branchdarpan.service.ReversalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reversals")
@RequiredArgsConstructor
public class ReversalController {

    private final ReversalService reversalService;

    @PostMapping
    public WorkflowRequestResponse create(
        @Valid @RequestBody ReversalCreateRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return reversalService.create(request, principal);
    }

    @GetMapping
    public List<WorkflowRequestResponse> list() {
        return reversalService.list();
    }

    @GetMapping("/{id}")
    public WorkflowRequestResponse get(@PathVariable Long id) {
        return reversalService.get(id);
    }

    @PutMapping("/{id}/approve")
    public WorkflowRequestResponse approve(
        @PathVariable Long id,
        @RequestBody WorkflowDecisionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return reversalService.approve(id, request, principal);
    }

    @PutMapping("/{id}/reject")
    public WorkflowRequestResponse reject(
        @PathVariable Long id,
        @RequestBody WorkflowDecisionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return reversalService.reject(id, request, principal);
    }
}
