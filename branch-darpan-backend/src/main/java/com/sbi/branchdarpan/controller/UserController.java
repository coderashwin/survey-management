package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.common.CommonDtos.ActionResponse;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.ApprovalRequest;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.HrmsUserResponse;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.UserRequestCreateRequest;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.UserRequestSummary;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.UserSummary;

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
import com.sbi.branchdarpan.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/hrms/{pfid}")
    public HrmsUserResponse hrms(@PathVariable String pfid) {
        return userService.fetchHrms(pfid);
    }

    @PostMapping("/users/request")
    public ActionResponse submitRequest(
        @Valid @RequestBody UserRequestCreateRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return userService.submitRequest(request, principal);
    }

    @GetMapping("/users/requests")
    public List<UserRequestSummary> getPendingRequests(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getPendingRequests(principal);
    }

    @PutMapping("/users/requests/{id}/approve")
    public ActionResponse approve(
        @PathVariable Long id,
        @RequestBody(required = false) ApprovalRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return userService.approveRequest(id, request == null ? null : request.remarks(), principal);
    }

    @PutMapping("/users/requests/{id}/reject")
    public ActionResponse reject(
        @PathVariable Long id,
        @RequestBody ApprovalRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return userService.rejectRequest(id, request.remarks(), principal);
    }

    @GetMapping("/users")
    public List<UserSummary> listUsers(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.listUsers(principal);
    }

    @GetMapping("/users/{id}")
    public UserSummary getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
