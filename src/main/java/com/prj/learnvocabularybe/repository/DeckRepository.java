package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<DeckEntity, Long> {
}
