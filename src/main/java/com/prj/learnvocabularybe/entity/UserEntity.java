package com.prj.learnvocabularybe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String email;
    @Column(nullable = false)
    private String password;
    private String avatarUrl;

    // Cài đặt nhắc học: bật/tắt và giờ nhắc (định dạng "HH:mm", ví dụ "08:00")
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean notificationEnabled = false;

    @Column(length = 5, columnDefinition = "varchar(5) default '08:00'")
    private String notificationTime = "08:00";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderEntity> folders = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
