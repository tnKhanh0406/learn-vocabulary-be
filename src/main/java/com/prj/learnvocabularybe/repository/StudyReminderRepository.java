package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.StudyReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyReminderRepository extends JpaRepository<StudyReminderEntity, Long> {

    /** Lấy tất cả lịch của user, sắp xếp theo giờ tăng dần */
    List<StudyReminderEntity> findByUserIdOrderByTimeAsc(Long userId);

    /** Kiểm tra user đã có lịch vào giờ này chưa */
    boolean existsByUserIdAndTime(Long userId, String time);

    /** Đếm số lịch của user (để giới hạn tối đa) */
    long countByUserId(Long userId);
}
