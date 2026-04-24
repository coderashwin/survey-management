package com.sbi.branchdarpan.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reversal_requests")
public class ReversalRequest extends MultiStageApprovalRequest {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_attempt_id", nullable = false)
    private SurveyAttempt surveyAttempt;

    @Column(name = "branch_code", nullable = false, length = 10)
    private String branchCode;

    @Lob
    @Column(nullable = false)
    private String reason;
}
