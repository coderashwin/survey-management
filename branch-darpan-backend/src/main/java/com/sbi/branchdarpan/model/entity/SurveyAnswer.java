package com.sbi.branchdarpan.model.entity;

import com.sbi.branchdarpan.model.enums.ApprovalDecisionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "survey_answers",
    uniqueConstraints = @UniqueConstraint(name = "uk_attempt_question", columnNames = { "attempt_id", "question_id" })
)
public class SurveyAnswer extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private SurveyAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Lob
    @Column(name = "answer_value")
    private String answerValue;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "branch_checker_status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalDecisionStatus branchCheckerStatus = ApprovalDecisionStatus.PENDING;

    @Lob
    @Column(name = "branch_checker_remarks")
    private String branchCheckerRemarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "rbo_checker_status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalDecisionStatus rboCheckerStatus = ApprovalDecisionStatus.PENDING;

    @Lob
    @Column(name = "rbo_checker_remarks")
    private String rboCheckerRemarks;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = false;
}
