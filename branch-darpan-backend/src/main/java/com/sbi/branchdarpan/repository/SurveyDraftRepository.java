package com.sbi.branchdarpan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.SurveyDraft;

public interface SurveyDraftRepository extends JpaRepository<SurveyDraft, Long> {

    Optional<SurveyDraft> findBySurveyIdAndUserIdAndBranchCode(Long surveyId, Long userId, String branchCode);

    void deleteBySurveyIdAndUserIdAndBranchCode(Long surveyId, Long userId, String branchCode);
}
