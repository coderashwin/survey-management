package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.common.CommonDtos.ActionResponse;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.HrmsUserResponse;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.UserRequestCreateRequest;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.UserRequestSummary;
import static com.sbi.branchdarpan.model.dto.user.UserDtos.UserSummary;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.exception.BadRequestException;
import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.User;
import com.sbi.branchdarpan.model.entity.UserRequest;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.model.enums.UserRequestStatus;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.repository.UserRequestRepository;
import com.sbi.branchdarpan.security.UserPrincipal;
import com.sbi.branchdarpan.util.RoleHierarchyUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRequestRepository userRequestRepository;
    private final HrmsService hrmsService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public HrmsUserResponse fetchHrms(String pfid) {
        return hrmsService.fetch(pfid);
    }

    public ActionResponse submitRequest(UserRequestCreateRequest request, UserPrincipal principal) {
        Role requestedRole = Role.valueOf(request.requestedRole());
        List<Role> allowedRoles = RoleHierarchyUtil.getAllowedRolesForCreation(principal.getRole());
        if (!allowedRoles.contains(requestedRole)) {
            throw new BadRequestException("You cannot create users for role " + requestedRole);
        }

        Role approverRole = RoleHierarchyUtil.getCheckerRole(principal.getRole(), requestedRole);
        UserRequest userRequest = UserRequest.builder()
            .pfid(request.pfid())
            .requestedRole(requestedRole)
            .name(request.name())
            .email(request.email())
            .mobile(request.mobile())
            .designation(request.designation())
            .circleCode(request.circleCode())
            .circleName(request.circleName())
            .aoCode(request.aoCode())
            .aoName(request.aoName())
            .rboCode(request.rboCode())
            .rboName(request.rboName())
            .branchCode(request.branchCode())
            .branchName(request.branchName())
            .status(UserRequestStatus.PENDING)
            .currentApproverRole(approverRole)
            .requestedBy(userRepository.findById(principal.getId()).orElseThrow())
            .build();

        UserRequest saved = userRequestRepository.save(userRequest);
        auditService.log(AuditRequestType.USER_REQUEST, saved.getId(), saved.getStatus().name(), principal, request.pfid(), "User request submitted");
        return new ActionResponse(saved.getId(), saved.getStatus().name(), "User creation request submitted");
    }

    @Transactional(readOnly = true)
    public List<UserRequestSummary> getPendingRequests(UserPrincipal principal) {
        return userRequestRepository.findByStatusAndCurrentApproverRoleOrderByUpdatedAtDesc(UserRequestStatus.PENDING, principal.getRole())
            .stream()
            .map(this::toRequestSummary)
            .toList();
    }

    public ActionResponse approveRequest(Long requestId, String remarks, UserPrincipal principal) {
        UserRequest userRequest = userRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("User request not found"));
        if (userRequest.getCurrentApproverRole() != principal.getRole()) {
            throw new BadRequestException("This request is not pending for your role");
        }

        User user = userRepository.findByPfid(userRequest.getPfid())
            .orElseGet(User::new);
        user.setPfid(userRequest.getPfid());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setMobile(userRequest.getMobile());
        user.setDesignation(userRequest.getDesignation());
        user.setRole(userRequest.getRequestedRole());
        user.setCircleCode(userRequest.getCircleCode());
        user.setCircleName(userRequest.getCircleName());
        user.setAoCode(userRequest.getAoCode());
        user.setAoName(userRequest.getAoName());
        user.setRboCode(userRequest.getRboCode());
        user.setRboName(userRequest.getRboName());
        user.setBranchCode(userRequest.getBranchCode());
        user.setBranchName(userRequest.getBranchName());
        user.setActive(true);
        userRepository.save(user);

        userRequest.setStatus(UserRequestStatus.APPROVED);
        userRequest.setRemarks(remarks);
        userRequest.setApprovedBy(userRepository.findById(principal.getId()).orElseThrow());
        auditService.log(AuditRequestType.USER_REQUEST, userRequest.getId(), userRequest.getStatus().name(), principal, userRequest.getPfid(), remarks);
        return new ActionResponse(userRequest.getId(), userRequest.getStatus().name(), "User request approved");
    }

    public ActionResponse rejectRequest(Long requestId, String remarks, UserPrincipal principal) {
        UserRequest userRequest = userRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("User request not found"));
        if (userRequest.getCurrentApproverRole() != principal.getRole()) {
            throw new BadRequestException("This request is not pending for your role");
        }
        userRequest.setStatus(UserRequestStatus.REJECTED);
        userRequest.setRemarks(remarks);
        userRequest.setApprovedBy(userRepository.findById(principal.getId()).orElseThrow());
        auditService.log(AuditRequestType.USER_REQUEST, userRequest.getId(), userRequest.getStatus().name(), principal, userRequest.getPfid(), remarks);
        return new ActionResponse(userRequest.getId(), userRequest.getStatus().name(), "User request rejected");
    }

    @Transactional(readOnly = true)
    public List<UserSummary> listUsers(UserPrincipal principal) {
        return userRepository.findAll().stream()
            .filter(user -> RoleHierarchyUtil.canAccessBranch(principal, user.getBranchCode() == null ? principal.getBranchCode() : user.getBranchCode()))
            .map(this::toUserSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserSummary getUser(Long id) {
        return toUserSummary(userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    private UserSummary toUserSummary(User user) {
        return new UserSummary(
            user.getId(),
            user.getPfid(),
            user.getName(),
            user.getEmail(),
            user.getMobile(),
            user.getDesignation(),
            user.getRole().name(),
            user.getCircleCode(),
            user.getCircleName(),
            user.getAoCode(),
            user.getAoName(),
            user.getRboCode(),
            user.getRboName(),
            user.getBranchCode(),
            user.getBranchName(),
            user.isActive()
        );
    }

    private UserRequestSummary toRequestSummary(UserRequest request) {
        return new UserRequestSummary(
            request.getId(),
            request.getPfid(),
            request.getRequestedRole().name(),
            request.getName(),
            request.getStatus().name(),
            request.getCurrentApproverRole() == null ? null : request.getCurrentApproverRole().name(),
            request.getCreatedAt().toString(),
            request.getRemarks()
        );
    }
}
