package com.prj.learnvocabularybe.dto.request;

import java.util.List;

public record DeckUpdateRequest(
        String name,
        String description,
        Boolean isPublic,
        List<WordUpdateRequest> words
) {
}
