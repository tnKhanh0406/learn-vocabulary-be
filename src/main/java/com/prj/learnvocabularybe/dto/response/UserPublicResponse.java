package com.prj.learnvocabularybe.dto.response;

import java.util.List;

public record UserPublicResponse(
        Long id,
        String username,
        String avatarUrl,
        List<DeckSummaryResponse> publicDecks,
        List<FolderSummaryResponse> publicFolders
) {
}
