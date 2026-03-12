package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}
