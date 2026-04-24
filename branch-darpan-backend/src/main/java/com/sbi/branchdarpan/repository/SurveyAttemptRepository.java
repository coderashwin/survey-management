package com.sbi.branchdarpan.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.SurveyAttempt;
import com.sbi.branchdarpan.model.enums.SurveyAttemptStatus;

public interface SurveyAttemptRepository extends JpaRepository<SurveyAttempt, Long> {

    Optional<SurveyAttempt> findFirstBySurveyIdAndBranchCodeAndStatusIn(
        Long surveyId,
        String branchCode,
        Collection<SurveyAttemptStatus> statuses
    );

    List<SurveyAttempt> findBySubmittedByIdOrderByUpdatedAtDesc(Long userId);

    List<SurveyAttempt> findByStatusInOrderByUpdatedAtDesc(List<SurveyAttemptStatus> statuses);
}
