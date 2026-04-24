package com.sbi.branchdarpan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
