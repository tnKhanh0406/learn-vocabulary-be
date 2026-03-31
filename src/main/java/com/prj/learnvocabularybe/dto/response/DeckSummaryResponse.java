package com.prj.learnvocabularybe.dto.response;

public record DeckSummaryResponse(
        Long id,
        String name,
        Long wordCount,
        String authorName,
        String avatarUrl
) {
}
