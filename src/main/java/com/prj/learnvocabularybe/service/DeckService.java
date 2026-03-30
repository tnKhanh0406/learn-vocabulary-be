package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.DeckUpdateRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DeckService {
    List<DeckSummaryResponse> getAllDecks();
    DeckResponse getDeckById(Long id);
    DeckResponse createDeck(DeckRequest deckRequest, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception;
    DeckResponse updateDeck(Long id, DeckUpdateRequest request, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception;
    void deleteDeck(Long id);
    List<DeckSummaryResponse> getAllDecksNotInFolder();
    DeckResponse addDeckToFolder(Long deckId, Long folderId);
}
