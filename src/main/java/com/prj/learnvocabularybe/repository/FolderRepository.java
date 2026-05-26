package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;
import com.prj.learnvocabularybe.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository cho FolderEntity và các truy vấn tổng hợp folder.
 */
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    /**
     * Lấy toàn bộ folder của một user.
     */
    List<FolderEntity> findAllByUserId(Long userId);

    /**
     * Lấy folder public của một user để hiển thị trên profile public.
     */
    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.FolderSummaryResponse(
            f.id,
            f.name,
            f.user.username
        )
        FROM FolderEntity f
        LEFT JOIN f.decks d
        WHERE f.user.id = :userId
        GROUP BY f.id, f.name, f.user.username
""")
    List<FolderSummaryResponse> searchPublicFoldersByUserId(Long userId);
}
