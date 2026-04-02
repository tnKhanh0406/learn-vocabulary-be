package com.prj.learnvocabularybe.dto.response;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        long deckCount,
        long folderCount
) {}
