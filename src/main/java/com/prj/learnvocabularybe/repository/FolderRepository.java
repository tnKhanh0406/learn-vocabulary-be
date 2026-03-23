package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    List<FolderEntity> findAllByUserId(Long userId);
}
