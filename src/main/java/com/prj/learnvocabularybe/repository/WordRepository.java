package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepository extends JpaRepository<WordEntity, Long> {
    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.WordResponse(
            w.id,
            w.english,
            w.vietnamese,
            w.imageUrl,
            w.audioUrl
        )
        FROM WordEntity w
        WHERE w.deck.id = :deckId
    """)
    List<WordResponse> findWordsByDeckId(Long deckId);
}
