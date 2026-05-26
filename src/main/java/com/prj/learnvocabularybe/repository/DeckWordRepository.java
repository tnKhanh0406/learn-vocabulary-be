package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.DeckWordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho bảng liên kết DeckWordEntity.
 */
public interface DeckWordRepository extends JpaRepository<DeckWordEntity, Long> {
}
