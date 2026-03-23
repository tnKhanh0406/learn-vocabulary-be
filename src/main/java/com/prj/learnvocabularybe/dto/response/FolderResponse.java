package com.prj.learnvocabularybe.dto.response;

import java.util.List;

public record FolderResponse(
        Long id,
        String name,
        String description,
        List<DeckSummaryResponse> decks
) {
}
