package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.common.CommonDtos.PagedResponse;
import static com.sbi.branchdarpan.model.dto.history.HistoryDtos.HistoryItem;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.service.HistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public PagedResponse<HistoryItem> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) AuditRequestType requestType
    ) {
        return historyService.getHistory(page, size, requestType);
    }

    @GetMapping("/{id}")
    public HistoryItem get(@PathVariable Long id) {
        return historyService.getById(id);
    }
}
