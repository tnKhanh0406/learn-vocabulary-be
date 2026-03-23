package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeckRepository extends JpaRepository<DeckEntity, Long> {
    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(w.id),
            d.user.username
        )
        FROM DeckEntity d
        LEFT JOIN d.words w
        WHERE d.folder.id = :folderId
        GROUP BY d.id, d.name, d.user.username
    """)
    List<DeckSummaryResponse> findDeckSummariesByFolderId(Long folderId);

    List<DeckEntity> findAllByUserId(Long userId);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.DeckSummaryResponse(
            d.id,
            d.name,
            COUNT(w),
            d.user.username
        )
        FROM DeckEntity d
        LEFT JOIN d.words w
        WHERE d.user.id = :userId
        GROUP BY d.id, d.name, d.user.username
    """)
    List<DeckSummaryResponse> getDeckSummariesByUserId(@Param("userId") Long userId);
}
