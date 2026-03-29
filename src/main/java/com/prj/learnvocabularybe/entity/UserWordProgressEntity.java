package com.prj.learnvocabularybe.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_word_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWordProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repetition_count", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer repetitionCount = 0;

    @Column(name = "interval_days", nullable = false) // Đổi tên thành interval_days để tránh trùng từ khóa SQL
    private Integer intervalDays;

    @Column(name = "ease_factor", nullable = false)
    private Float easeFactor;

    @Column(name = "lapses", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer lapses = 0;

    @Column(name = "last_review_date")
    private LocalDateTime lastReviewDate;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    // Khóa ngoại liên kết với WordMeaning
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_meaning_id")
    private WordMeaningEntity wordMeaning;

    // Khóa ngoại liên kết với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}