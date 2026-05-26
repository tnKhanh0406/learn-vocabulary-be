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

/**
 * Repository cho thao tác với DeckEntity và các truy vấn tổng hợp deck.
 */
public interface DeckRepository extends JpaRepository<DeckEntity, Long> {
    /**
     * Tìm deck theo chủ đề của user, dùng khi chat AI yêu cầu deck theo topic.
     */
    Optional<DeckEntity> findFirstByUser_IdAndTopicIgnoreCase(Long userId, String topic);

    /**
     * Lấy danh sách deck trong folder kèm số lượng từ và thông tin chủ sở hữu.
     */
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

    /**
     * Tìm deck của chính người dùng hiện tại theo tên.
     */
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

    /**
     * Lấy các deck của user hiện tại chưa nằm trong folder nào.
     */
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

    /**
     * Gỡ một deck khỏi folder.
     */
    @Modifying
    @Transactional
    @Query("""
    UPDATE DeckEntity d
    SET d.folder.id = NULL
    WHERE d.id = :deckId
""")
    void removeDeckFromFolder(Long deckId);

    /**
     * Gắn một deck vào folder theo id deck và id folder.
     */
    @Modifying
    @Transactional
    @Query("""
    UPDATE DeckEntity d
    SET d.folder.id = :folderId
    WHERE d.id = :deckId
""")
    void addDeckToFolder(Long deckId, Long folderId);

    /**
     * Lấy deck kèm toàn bộ wordMeaning đã fetch sẵn để sao chép deck.
     */
    @Query("""
        SELECT DISTINCT d
        FROM DeckEntity d
        LEFT JOIN FETCH d.deckWords dw
        LEFT JOIN FETCH dw.wordMeaning wm
        LEFT JOIN FETCH wm.vocabulary v
        WHERE d.id = :deckId
    """)
    Optional<DeckEntity> findByIdWithWords(Long deckId);

        /**
         * Tìm deck public của người khác theo tên để phục vụ tìm kiếm.
         */
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

        /**
         * Lấy các deck public nổi bật để gợi ý học tập.
         */
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

        /**
         * Lấy các deck public của một user khác.
         */
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

        /**
         * Lấy các deck public theo folder để hiển thị trang public folder.
         */
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
