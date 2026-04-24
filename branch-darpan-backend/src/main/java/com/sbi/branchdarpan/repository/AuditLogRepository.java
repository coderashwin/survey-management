package com.sbi.branchdarpan.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.AuditLog;
import com.sbi.branchdarpan.model.enums.AuditRequestType;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByRequestType(AuditRequestType requestType, Pageable pageable);
}
