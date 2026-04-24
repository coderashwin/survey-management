package com.sbi.branchdarpan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findByActiveTrue();

    List<Survey> findAllByOrderByStartDateDesc();
}
