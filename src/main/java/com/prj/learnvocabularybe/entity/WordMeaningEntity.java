package com.prj.learnvocabularybe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "word_meanings")
@Entity
@Getter
@Setter
public class WordMeaningEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    String meaning;

    String imageUrl;

    @Column(columnDefinition = "TEXT")
    String explanation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "vocab_id", nullable = false)
    private VocabularyEntity vocabulary;
    @Column(name = "ai_explanation_cache", columnDefinition = "TEXT")
    private String aiExplanationCache;
}
