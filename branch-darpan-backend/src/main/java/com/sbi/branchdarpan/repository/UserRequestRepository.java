package com.sbi.branchdarpan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.UserRequest;
import com.sbi.branchdarpan.model.enums.Role;
import com.sbi.branchdarpan.model.enums.UserRequestStatus;

public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {

    List<UserRequest> findByStatusAndCurrentApproverRoleOrderByUpdatedAtDesc(
        UserRequestStatus status,
        Role currentApproverRole
    );
}
