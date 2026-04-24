package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.dashboard.DashboardDtos.DashboardSummary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardSummary dashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/public/dashboard")
    public DashboardSummary publicDashboard() {
        return dashboardService.getDashboard();
    }
}
