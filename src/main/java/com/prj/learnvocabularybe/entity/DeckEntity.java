package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @ManyToOne
    @JoinColumn(name = "copied_from_deck_id")
    private DeckEntity copiedFromDeck;

    @OneToMany
    @JoinColumn(name = "deck_id")
    private List<WordEntity> words;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
