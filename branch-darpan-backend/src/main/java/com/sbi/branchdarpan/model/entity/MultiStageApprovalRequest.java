package com.sbi.branchdarpan.model.entity;

import java.time.Instant;

import com.sbi.branchdarpan.model.enums.ApprovalDecisionStatus;
import com.sbi.branchdarpan.model.enums.WorkflowRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Lob;

@MappedSuperclass
public abstract class MultiStageApprovalRequest extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowRequestStatus status = WorkflowRequestStatus.PENDING_CIRCLE_CHECKER;

    @Enumerated(EnumType.STRING)
    @Column(name = "circle_checker_status", nullable = false, length = 20)
    private ApprovalDecisionStatus circleCheckerStatus = ApprovalDecisionStatus.PENDING;

    @Lob
    @Column(name = "circle_checker_remarks")
    private String circleCheckerRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_checker_acted_by")
    private User circleCheckerActedBy;

    @Column(name = "circle_checker_acted_at")
    private Instant circleCheckerActedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cc_maker_status", nullable = false, length = 20)
    private ApprovalDecisionStatus ccMakerStatus = ApprovalDecisionStatus.PENDING;

    @Lob
    @Column(name = "cc_maker_remarks")
    private String ccMakerRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_maker_acted_by")
    private User ccMakerActedBy;

    @Column(name = "cc_maker_acted_at")
    private Instant ccMakerActedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cc_checker_status", nullable = false, length = 20)
    private ApprovalDecisionStatus ccCheckerStatus = ApprovalDecisionStatus.PENDING;

    @Lob
    @Column(name = "cc_checker_remarks")
    private String ccCheckerRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cc_checker_acted_by")
    private User ccCheckerActedBy;

    @Column(name = "cc_checker_acted_at")
    private Instant ccCheckerActedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;

    public WorkflowRequestStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowRequestStatus status) {
        this.status = status;
    }

    public ApprovalDecisionStatus getCircleCheckerStatus() {
        return circleCheckerStatus;
    }

    public void setCircleCheckerStatus(ApprovalDecisionStatus circleCheckerStatus) {
        this.circleCheckerStatus = circleCheckerStatus;
    }

    public String getCircleCheckerRemarks() {
        return circleCheckerRemarks;
    }

    public void setCircleCheckerRemarks(String circleCheckerRemarks) {
        this.circleCheckerRemarks = circleCheckerRemarks;
    }

    public User getCircleCheckerActedBy() {
        return circleCheckerActedBy;
    }

    public void setCircleCheckerActedBy(User circleCheckerActedBy) {
        this.circleCheckerActedBy = circleCheckerActedBy;
    }

    public Instant getCircleCheckerActedAt() {
        return circleCheckerActedAt;
    }

    public void setCircleCheckerActedAt(Instant circleCheckerActedAt) {
        this.circleCheckerActedAt = circleCheckerActedAt;
    }

    public ApprovalDecisionStatus getCcMakerStatus() {
        return ccMakerStatus;
    }

    public void setCcMakerStatus(ApprovalDecisionStatus ccMakerStatus) {
        this.ccMakerStatus = ccMakerStatus;
    }

    public String getCcMakerRemarks() {
        return ccMakerRemarks;
    }

    public void setCcMakerRemarks(String ccMakerRemarks) {
        this.ccMakerRemarks = ccMakerRemarks;
    }

    public User getCcMakerActedBy() {
        return ccMakerActedBy;
    }

    public void setCcMakerActedBy(User ccMakerActedBy) {
        this.ccMakerActedBy = ccMakerActedBy;
    }

    public Instant getCcMakerActedAt() {
        return ccMakerActedAt;
    }

    public void setCcMakerActedAt(Instant ccMakerActedAt) {
        this.ccMakerActedAt = ccMakerActedAt;
    }

    public ApprovalDecisionStatus getCcCheckerStatus() {
        return ccCheckerStatus;
    }

    public void setCcCheckerStatus(ApprovalDecisionStatus ccCheckerStatus) {
        this.ccCheckerStatus = ccCheckerStatus;
    }

    public String getCcCheckerRemarks() {
        return ccCheckerRemarks;
    }

    public void setCcCheckerRemarks(String ccCheckerRemarks) {
        this.ccCheckerRemarks = ccCheckerRemarks;
    }

    public User getCcCheckerActedBy() {
        return ccCheckerActedBy;
    }

    public void setCcCheckerActedBy(User ccCheckerActedBy) {
        this.ccCheckerActedBy = ccCheckerActedBy;
    }

    public Instant getCcCheckerActedAt() {
        return ccCheckerActedAt;
    }

    public void setCcCheckerActedAt(Instant ccCheckerActedAt) {
        this.ccCheckerActedAt = ccCheckerActedAt;
    }

    public User getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(User initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
}
