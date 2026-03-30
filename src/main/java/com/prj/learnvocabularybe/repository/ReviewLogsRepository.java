package com.prj.learnvocabularybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prj.learnvocabularybe.entity.ReviewLogsEntity;

@Repository
public interface ReviewLogsRepository extends JpaRepository<ReviewLogsEntity, Long> {
}