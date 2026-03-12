package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
}
