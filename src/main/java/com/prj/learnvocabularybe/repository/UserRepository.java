package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
