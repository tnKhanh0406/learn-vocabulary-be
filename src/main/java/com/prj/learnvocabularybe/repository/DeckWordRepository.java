package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.DeckWordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckWordRepository extends JpaRepository<DeckWordEntity, Long> {
}
