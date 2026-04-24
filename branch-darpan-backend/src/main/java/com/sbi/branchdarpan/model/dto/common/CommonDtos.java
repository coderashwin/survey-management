package com.sbi.branchdarpan.model.dto.common;

import java.util.List;

public final class CommonDtos {

    private CommonDtos() {
    }

    public record ActionResponse(Long id, String status, String message) {
    }

    public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage
    ) {
    }
}
