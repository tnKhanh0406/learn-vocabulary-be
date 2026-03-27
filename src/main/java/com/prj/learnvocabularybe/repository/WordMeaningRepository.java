package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordMeaningRepository extends JpaRepository<WordMeaningEntity, Long> {
    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.WordResponse(
            wm.id,
            v.word,
            wm.meaning,
            wm.imageUrl,
            v.audioUrl
        )
        FROM DeckWordEntity dw
        JOIN dw.wordMeaning wm
        JOIN wm.vocabulary v
        WHERE dw.deck.id = :deckId
        ORDER BY dw.id
    """)
    List<WordResponse> findWordResponsesByDeckId(Long deckId);
}
