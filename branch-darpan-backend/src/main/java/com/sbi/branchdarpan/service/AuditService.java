package com.sbi.branchdarpan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.model.entity.AuditLog;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.repository.AuditLogRepository;
import com.sbi.branchdarpan.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(
        AuditRequestType type,
        Long referenceId,
        String status,
        UserPrincipal actor,
        String targetPfid,
        String remarks
    ) {
        auditLogRepository.save(AuditLog.builder()
            .requestType(type)
            .referenceId(referenceId)
            .status(status)
            .actorPfid(actor == null ? "SYSTEM" : actor.getPfid())
            .actorName(actor == null ? "System" : actor.getDisplayName())
            .actorRole(actor == null ? null : actor.getRole())
            .targetPfid(targetPfid)
            .remarks(remarks)
            .build());
    }
}
