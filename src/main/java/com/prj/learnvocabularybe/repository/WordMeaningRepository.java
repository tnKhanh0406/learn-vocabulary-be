package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository cho WordMeaningEntity và các truy vấn trả DTO từ deck.
 */
public interface WordMeaningRepository extends JpaRepository<WordMeaningEntity, Long> {
    /**
     * Lấy danh sách từ của một deck dưới dạng DTO hiển thị.
     */
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

        /**
         * Xóa các word meaning không còn được deck nào tham chiếu.
         */
    @Modifying
    @Query("""
    DELETE FROM WordMeaningEntity w
    WHERE w.id IN :ids
    AND NOT EXISTS (
        SELECT 1 FROM DeckWordEntity dw
        WHERE dw.wordMeaning.id = w.id
    )
""")
    void deleteUnusedWordMeanings(@Param("ids") Set<Long> ids);
}
