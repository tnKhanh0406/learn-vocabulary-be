package com.prj.learnvocabularybe.dto.response;

public record SearchUserResponse(
        Long id,
        String username,
        String avatarUrl,
        Long deckCount,
        Long folderCount
) {
}
