package com.prj.learnvocabularybe.mapper;

import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.WordResponse;
import com.prj.learnvocabularybe.entity.DeckEntity;

import java.util.List;

public class DeckMapper {
    public static DeckResponse map(DeckEntity deckEntity, List<WordResponse> wordResponses) {
        return new DeckResponse(
                deckEntity.getId(),
                deckEntity.getName(),
                deckEntity.getDescription(),
                deckEntity.getIsPublic(),
                deckEntity.getUser().getUsername(),
                deckEntity.getUser().getAvatarUrl(),
                wordResponses
        );
    }
}
