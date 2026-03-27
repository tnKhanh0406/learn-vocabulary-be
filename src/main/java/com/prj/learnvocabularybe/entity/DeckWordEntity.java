package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "deck_words")
@Entity
@Getter
@Setter
public class DeckWordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    @ManyToOne
    @JoinColumn(name = "word_meaning_id", nullable = false)
    private WordMeaningEntity wordMeaning;
}
