package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lưu các lịch nhắc học của người dùng.
 * Mỗi user có thể có nhiều lịch (vd: 08:00, 14:00, 21:00).
 * Unique constraint (user_id, time) đảm bảo không trùng giờ.
 */
@Entity
@Table(
    name = "study_reminders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "time"})
)
@Getter
@Setter
public class StudyReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** Giờ nhắc, định dạng "HH:mm" — vd: "08:30" */
    @Column(nullable = false, length = 5)
    private String time;

    /** Bật/tắt từng lịch riêng lẻ */
    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
