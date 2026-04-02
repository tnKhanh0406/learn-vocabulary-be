package com.prj.learnvocabularybe.dto.response;

import java.util.List;

public record FolderPublicResponse(
        Long id,
        String name,
        String authorName,
        String authorAvatarUrl,
        List<DeckSummaryResponse> decks
) {
}
