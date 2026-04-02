package com.prj.learnvocabularybe.dto.response;

public record AuthResponse(
        String token,
        Long id,
        String username,
        String email,
        String avatarUrl
) {}
