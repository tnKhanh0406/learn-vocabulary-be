package com.prj.learnvocabularybe.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prj.learnvocabularybe.entity.UserWordProgressEntity;

/**
 * Repository cho UserWordProgressEntity.
 */
@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgressEntity, Long> {

    /**
     * Lấy tiến độ của một user với một từ vựng cụ thể.
     */
    Optional<UserWordProgressEntity> findByUserIdAndWordMeaningId(Long userId, Long wordMeaningId);

    /**
     * Lấy danh sách từ cần ôn tập hôm nay: nextReviewDate <= thời gian hiện tại.
     */
    @Query("SELECT u FROM UserWordProgressEntity u WHERE u.user.id = :userId AND u.nextReviewDate <= :now")
    List<UserWordProgressEntity> findWordsToReviewToday(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Lấy danh sách các từ hay quên, phục vụ minigame hoặc dashboard.
     */
    @Query("SELECT u FROM UserWordProgressEntity u WHERE u.user.id = :userId AND u.lapses > :lapsesThreshold")
    List<UserWordProgressEntity> findForgottenWords(@Param("userId") Long userId, @Param("lapsesThreshold") Integer lapsesThreshold);

    /**
     * Đếm tổng số từ mà user đang theo dõi.
     */
    long countByUserId(Long userId);

    /**
     * Đếm số từ mà user đã ghi nhớ tốt theo ngưỡng repetition/lapses.
     */
    @Query("""
        SELECT COUNT(u)
        FROM UserWordProgressEntity u
        WHERE u.user.id = :userId
          AND COALESCE(u.repetitionCount, 0) >= 2
          AND COALESCE(u.lapses, 0) <= 1
    """)
    long countRememberedWords(@Param("userId") Long userId);
}