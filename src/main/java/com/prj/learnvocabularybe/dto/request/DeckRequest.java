package com.prj.learnvocabularybe.dto.request;

import java.util.List;

public record DeckRequest(
    String name,
    String description,
    Boolean isPublic,
    List<WordRequest> words
) {
}
