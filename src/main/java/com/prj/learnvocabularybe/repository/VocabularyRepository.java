package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.VocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {
    Optional<VocabularyEntity> findByWordIgnoreCase(String word);
    List<VocabularyEntity> findAllByWordInIgnoreCase(Set<String> words);
}
