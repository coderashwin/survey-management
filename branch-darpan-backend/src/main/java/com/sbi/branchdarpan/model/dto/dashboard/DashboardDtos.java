package com.sbi.branchdarpan.model.dto.dashboard;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record DashboardSummary(
        long totalUsers,
        long pendingUserRequests,
        long pendingSurveyApprovals,
        long approvedSurveys,
        String activeSurveyTitle
    ) {
    }
}
