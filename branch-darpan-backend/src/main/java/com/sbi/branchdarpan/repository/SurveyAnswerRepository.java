package com.sbi.branchdarpan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.SurveyAnswer;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    List<SurveyAnswer> findByAttemptId(Long attemptId);

    Optional<SurveyAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    void deleteByAttemptId(Long attemptId);
}
