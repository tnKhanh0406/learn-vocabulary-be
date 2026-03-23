package com.prj.learnvocabularybe.dto.response;

import java.util.List;

public record DeckResponse(
    Long id,
    String name,
    String description,
    Boolean isPublic,
    String username,
    String avatarUrl,
    List<WordResponse> words
) {
}
