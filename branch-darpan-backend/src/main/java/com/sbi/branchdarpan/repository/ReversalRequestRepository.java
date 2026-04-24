package com.sbi.branchdarpan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.ReversalRequest;
import com.sbi.branchdarpan.model.enums.WorkflowRequestStatus;

public interface ReversalRequestRepository extends JpaRepository<ReversalRequest, Long> {

    List<ReversalRequest> findByStatusIn(List<WorkflowRequestStatus> statuses);
}
