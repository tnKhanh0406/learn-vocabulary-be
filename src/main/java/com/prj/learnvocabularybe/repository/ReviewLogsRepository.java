package com.prj.learnvocabularybe.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prj.learnvocabularybe.entity.ReviewLogsEntity;

@Repository
public interface ReviewLogsRepository extends JpaRepository<ReviewLogsEntity, Long> {
	@Query("""
		SELECT DISTINCT DATE(r.reviewedAt)
		FROM ReviewLogsEntity r
		WHERE r.user.id = :userId
		ORDER BY DATE(r.reviewedAt) DESC
	""")
	List<LocalDate> findDistinctReviewDates(@Param("userId") Long userId);
}