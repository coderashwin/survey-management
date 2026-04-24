package com.sbi.branchdarpan.model.dto.history;

public final class HistoryDtos {

    private HistoryDtos() {
    }

    public record HistoryItem(
        Long id,
        String requestType,
        Long referenceId,
        String status,
        String actorPfid,
        String actorName,
        String actorRole,
        String targetPfid,
        String remarks,
        String createdAt
    ) {
    }
}
