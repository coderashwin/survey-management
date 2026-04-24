package com.sbi.branchdarpan.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<String> details,
    Instant timestamp
) {
}
