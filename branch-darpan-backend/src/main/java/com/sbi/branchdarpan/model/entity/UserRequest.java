package com.sbi.branchdarpan.model.entity;

import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.model.enums.UserRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_requests")
public class UserRequest extends AuditableEntity {

    @Column(nullable = false, length = 20)
    private String pfid;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false, length = 30)
    private Role requestedRole;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(length = 15)
    private String mobile;

    @Column(length = 100)
    private String designation;

    @Column(name = "circle_code", length = 10)
    private String circleCode;

    @Column(name = "circle_name", length = 100)
    private String circleName;

    @Column(name = "ao_code", length = 10)
    private String aoCode;

    @Column(name = "ao_name", length = 100)
    private String aoName;

    @Column(name = "rbo_code", length = 10)
    private String rboCode;

    @Column(name = "rbo_name", length = 100)
    private String rboName;

    @Column(name = "branch_code", length = 10)
    private String branchCode;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UserRequestStatus status = UserRequestStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_approver_role", length = 30)
    private Role currentApproverRole;

    @Lob
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
}
