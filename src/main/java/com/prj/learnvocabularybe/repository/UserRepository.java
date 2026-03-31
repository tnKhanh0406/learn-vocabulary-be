package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

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
