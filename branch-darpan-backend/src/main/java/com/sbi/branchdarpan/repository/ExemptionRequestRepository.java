package com.sbi.branchdarpan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.ExemptionRequest;
import com.sbi.branchdarpan.model.enums.WorkflowRequestStatus;

public interface ExemptionRequestRepository extends JpaRepository<ExemptionRequest, Long> {

    List<ExemptionRequest> findByStatusIn(List<WorkflowRequestStatus> statuses);

    boolean existsBySurveyIdAndBranchCodeAndStatus(Long surveyId, String branchCode, WorkflowRequestStatus status);
}
