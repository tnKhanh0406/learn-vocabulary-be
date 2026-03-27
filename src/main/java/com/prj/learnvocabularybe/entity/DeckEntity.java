package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "decks")
@Entity
@Getter
@Setter
public class DeckEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @ManyToOne
    @JoinColumn(name = "copied_from_deck_id")
    private DeckEntity copiedFromDeck;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(nullable = false)
    private Boolean isGeneratedByAI = false;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckWordEntity> deckWords = new ArrayList<>();

    public void addDeckWord(WordMeaningEntity meaning) {
        DeckWordEntity dw = new DeckWordEntity();
        dw.setDeck(this);
        dw.setWordMeaning(meaning);
        this.deckWords.add(dw);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isGeneratedByAI = false;
    }
}
