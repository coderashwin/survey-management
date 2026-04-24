package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.dashboard.DashboardDtos.DashboardSummary;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.model.enums.SurveyAttemptStatus;
import com.sbi.branchdarpan.model.enums.UserRequestStatus;
import com.sbi.branchdarpan.repository.SurveyAttemptRepository;
import com.sbi.branchdarpan.repository.SurveyRepository;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.repository.UserRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final UserRequestRepository userRequestRepository;
    private final SurveyAttemptRepository surveyAttemptRepository;
    private final SurveyRepository surveyRepository;

    public DashboardSummary getDashboard() {
        long pendingApprovals = surveyAttemptRepository.findByStatusInOrderByUpdatedAtDesc(List.of(
            SurveyAttemptStatus.PENDING_BRANCH_CHECKER,
            SurveyAttemptStatus.PENDING_RBO_CHECKER
        )).size();

        long approvedSurveys = surveyAttemptRepository.findByStatusInOrderByUpdatedAtDesc(List.of(
            SurveyAttemptStatus.APPROVED
        )).size();

        long pendingRequests = userRequestRepository.findAll().stream()
            .filter(request -> request.getStatus() == UserRequestStatus.PENDING)
            .count();

        String activeSurveyTitle = surveyRepository.findByActiveTrue().map(s -> s.getTitle()).orElse(null);

        return new DashboardSummary(
            userRepository.count(),
            pendingRequests,
            pendingApprovals,
            approvedSurveys,
            activeSurveyTitle
        );
    }
}
