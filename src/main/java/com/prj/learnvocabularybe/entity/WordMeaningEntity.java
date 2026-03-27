package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
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
}
