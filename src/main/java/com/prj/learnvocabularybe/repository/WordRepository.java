package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<WordEntity, Long> {
}
