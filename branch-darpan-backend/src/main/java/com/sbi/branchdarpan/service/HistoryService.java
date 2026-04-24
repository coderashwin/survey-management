package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.common.CommonDtos.PagedResponse;
import static com.sbi.branchdarpan.model.dto.history.HistoryDtos.HistoryItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.model.entity.AuditLog;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HistoryService {

    private final AuditLogRepository auditLogRepository;

    public PagedResponse<HistoryItem> getHistory(int page, int size, AuditRequestType requestType) {
        Page<AuditLog> result = requestType == null
            ? auditLogRepository.findAll(PageRequest.of(page, size))
            : auditLogRepository.findByRequestType(requestType, PageRequest.of(page, size));

        return new PagedResponse<>(
            result.getContent().stream().map(this::map).toList(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.getNumber()
        );
    }

    public HistoryItem getById(Long id) {
        return map(auditLogRepository.findById(id).orElseThrow());
    }

    private HistoryItem map(AuditLog log) {
        return new HistoryItem(
            log.getId(),
            log.getRequestType().name(),
            log.getReferenceId(),
            log.getStatus(),
            log.getActorPfid(),
            log.getActorName(),
            log.getActorRole() == null ? null : log.getActorRole().name(),
            log.getTargetPfid(),
            log.getRemarks(),
            log.getCreatedAt().toString()
        );
    }
}
