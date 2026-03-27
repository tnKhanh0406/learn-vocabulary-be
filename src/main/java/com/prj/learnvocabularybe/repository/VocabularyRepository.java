package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.VocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {
    Optional<VocabularyEntity> findByWordIgnoreCase(String word);
}
