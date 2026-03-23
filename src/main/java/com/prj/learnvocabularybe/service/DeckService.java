package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DeckService {
    List<DeckSummaryResponse> getAllDecks();
    DeckResponse createDeck(DeckRequest deckRequest, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception;
}
