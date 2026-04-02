package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // Lấy 20 thông báo gần nhất của user
    List<NotificationEntity> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    // Đếm thông báo chưa đọc
    long countByUserIdAndIsReadFalse(Long userId);

    // Đánh dấu tất cả là đã đọc
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
}
