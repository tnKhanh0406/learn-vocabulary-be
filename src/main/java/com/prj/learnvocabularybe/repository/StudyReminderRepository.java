package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.StudyReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository cho StudyReminderEntity.
 */
public interface StudyReminderRepository extends JpaRepository<StudyReminderEntity, Long> {

    /**
     * Lấy tất cả lịch nhắc của user, sắp xếp theo giờ tăng dần.
     */
    List<StudyReminderEntity> findByUserIdOrderByTimeAsc(Long userId);

    /**
     * Kiểm tra user đã có lịch vào cùng giờ chưa.
     */
    boolean existsByUserIdAndTime(Long userId, String time);

    /**
     * Đếm số lịch của user để áp giới hạn tối đa.
     */
    long countByUserId(Long userId);
}
