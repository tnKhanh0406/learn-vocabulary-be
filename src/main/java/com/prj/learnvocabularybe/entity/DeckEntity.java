package com.prj.learnvocabularybe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
    
    @Column(name = "is_ai_generated", columnDefinition = "integer default 0")
    private Integer isAiGenerated = 0;

    @Column(name = "topic", length = 255)
    private String topic;

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
