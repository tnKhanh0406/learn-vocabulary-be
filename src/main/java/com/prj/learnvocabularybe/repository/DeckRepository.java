package com.prj.learnvocabularybe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.entity.DeckEntity;

public interface DeckRepository extends JpaRepository<DeckEntity, Long> {
    Optional<DeckEntity> findFirstByUser_IdAndTopicIgnoreCase(Long userId, String topic);

    @Query("""
    SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
        d.id,
        d.name,
        COUNT(dw.id),
        d.user.username,
        d.user.avatarUrl
    )
    FROM DeckEntity d
    LEFT JOIN d.deckWords dw
    WHERE d.folder.id = :folderId
    GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl
""")
    List<DeckSummaryResponse> findDeckSummariesByFolderId(Long folderId);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(dw.id),
            d.user.username,
            d.user.avatarUrl
        )
        FROM DeckEntity d
        LEFT JOIN d.deckWords dw
        WHERE d.user.id = :userId
          AND (:q IS NULL OR :q = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')))
        GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl
        ORDER BY d.createdAt DESC
    """)
    List<DeckSummaryResponse> searchMyDecksByName(@Param("userId") Long userId,
                                                  @Param("q") String q);

    @Modifying
    @Transactional
    @Query("""
        UPDATE DeckEntity d
        SET d.folder.id = :folderId
        WHERE d.id IN :deckIds
    """)
    void addDecksToFolder(Long folderId, List<Long> deckIds);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(dw.id),
            d.user.username,
            d.user.avatarUrl
        )
        FROM DeckEntity d
        LEFT JOIN d.deckWords dw
        WHERE d.user.id = :userId AND d.folder IS NULL
        GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl
""")
    List<DeckSummaryResponse> getDeckSummariesNotInFolderByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("""
    UPDATE DeckEntity d
    SET d.folder.id = NULL
    WHERE d.id = :deckId
""")
    void removeDeckFromFolder(Long deckId);

    @Modifying
    @Transactional
    @Query("""
    UPDATE DeckEntity d
    SET d.folder.id = :folderId
    WHERE d.id = :deckId
""")
    void addDeckToFolder(Long deckId, Long folderId);

    @Query("""
        SELECT DISTINCT d
        FROM DeckEntity d
        LEFT JOIN FETCH d.deckWords dw
        LEFT JOIN FETCH dw.wordMeaning wm
        LEFT JOIN FETCH wm.vocabulary v
        WHERE d.id = :deckId
    """)
    Optional<DeckEntity> findByIdWithWords(Long deckId);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(dw.id),
            d.user.username,
            d.user.avatarUrl
        )
        FROM DeckEntity d
        LEFT JOIN d.deckWords dw
        WHERE d.user.id <> :userId
          AND d.isPublic = true
          AND (:q IS NULL OR :q = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')))
        GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl
        ORDER BY d.createdAt DESC
    """)
    List<DeckSummaryResponse> searchPublicDecksByName(@Param("userId") Long userId,
                                                      @Param("q") String q);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(dw.id),
            d.user.username,
            d.user.avatarUrl
        )
        FROM DeckEntity d
        LEFT JOIN d.deckWords dw
        WHERE d.user.id <> :userId
          AND d.isPublic = true
        GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl, d.createdAt
        ORDER BY COUNT(dw.id) DESC, d.createdAt DESC
    """)
    List<DeckSummaryResponse> findTopRecommendedPublicDecks(@Param("userId") Long userId);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(dw.id),
            d.user.username,
            d.user.avatarUrl
        )
        FROM DeckEntity d
        LEFT JOIN d.deckWords dw
        WHERE d.user.id = :userId
          AND d.isPublic = true
        GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl
        ORDER BY d.createdAt DESC
    """)
    List<DeckSummaryResponse> searchPublicDecksByUserId(Long userId);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(dw.id),
            d.user.username,
            d.user.avatarUrl
        )
        FROM DeckEntity d
        LEFT JOIN d.deckWords dw
        WHERE d.folder.id = :folderId
          AND d.isPublic = true
        GROUP BY d.id, d.name, d.user.username, d.user.avatarUrl
    """)
    List<DeckSummaryResponse> findDeckSummariesPublicByFolderId(Long folderId);
}
