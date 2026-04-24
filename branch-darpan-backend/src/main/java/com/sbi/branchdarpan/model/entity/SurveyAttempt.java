package com.sbi.branchdarpan.model.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sbi.branchdarpan.model.enums.SurveyAttemptStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
@Table(name = "survey_attempts")
public class SurveyAttempt extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "branch_code", nullable = false, length = 10)
    private String branchCode;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private SurveyAttemptStatus status = SurveyAttemptStatus.DRAFT;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private int attemptNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_checker_id")
    private User branchChecker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbo_checker_id")
    private User rboChecker;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "branch_checker_acted_at")
    private Instant branchCheckerActedAt;

    @Column(name = "rbo_checker_acted_at")
    private Instant rboCheckerActedAt;

    @Builder.Default
    @OrderBy("question.id asc")
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswer> answers = new ArrayList<>();
}
