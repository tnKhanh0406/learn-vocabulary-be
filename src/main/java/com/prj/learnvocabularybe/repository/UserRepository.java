package com.prj.learnvocabularybe.repository;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho UserEntity và các truy vấn tổng hợp liên quan đến hồ sơ người dùng.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Tìm người dùng theo username.
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Tìm người dùng theo email.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại hay chưa.
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại hay chưa.
     */
    boolean existsByEmail(String email);

    /**
     * Đếm số deck thuộc về user.
     */
    @Query("SELECT COUNT(d) FROM DeckEntity d WHERE d.user.id = :userId")
    long countDecksByUserId(@Param("userId") Long userId);

    /**
     * Đếm số folder thuộc về user.
     */
    @Query("SELECT COUNT(f) FROM FolderEntity f WHERE f.user.id = :userId")
    long countFoldersByUserId(@Param("userId") Long userId);

    /**
     * Tìm user theo username để hiển thị danh sách public profile.
     */
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
