package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "words")
public class WordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String english;

    private String vietnamese;
}
