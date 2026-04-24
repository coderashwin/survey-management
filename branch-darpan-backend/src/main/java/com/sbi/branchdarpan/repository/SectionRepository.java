package com.sbi.branchdarpan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbi.branchdarpan.model.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
