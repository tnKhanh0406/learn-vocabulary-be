package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.VocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository cho VocabularyEntity.
 */
public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {
    /**
     * Tìm vocabulary theo từ tiếng Anh không phân biệt hoa thường.
     */
    Optional<VocabularyEntity> findByWordIgnoreCase(String word);

    /**
     * Tìm tất cả vocabulary theo tập từ, không phân biệt hoa thường.
     */
    List<VocabularyEntity> findAllByWordInIgnoreCase(Set<String> words);
}
