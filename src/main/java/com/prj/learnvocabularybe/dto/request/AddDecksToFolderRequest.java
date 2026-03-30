package com.prj.learnvocabularybe.dto.request;

import java.util.List;

public record AddDecksToFolderRequest(
        List<Long> deckIds
) {
}