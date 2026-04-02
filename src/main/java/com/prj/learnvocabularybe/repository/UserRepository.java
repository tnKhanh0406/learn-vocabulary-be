package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(d) FROM DeckEntity d WHERE d.user.id = :userId")
    long countDecksByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM FolderEntity f WHERE f.user.id = :userId")
    long countFoldersByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT new com.prj.learnvocabularybe.dto.response.SearchUserResponse(
            u.id,
            u.username,
            u.avatarUrl,
            (SELECT CAST(COUNT(d) AS Long) FROM DeckEntity d WHERE d.user.id = u.id),
            (SELECT COUNT(f) FROM FolderEntity f WHERE f.user.id = u.id)
        )
        FROM UserEntity u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
        AND u.id <> :userId
""")
    List<SearchUserResponse> searchUsersByUsername(String q, Long userId);
}
