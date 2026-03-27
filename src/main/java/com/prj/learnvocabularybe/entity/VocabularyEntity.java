package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "vocabulary")
@Entity
@Getter
@Setter
public class VocabularyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    String word;

    String audioUrl;
}
