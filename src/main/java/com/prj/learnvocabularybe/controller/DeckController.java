package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.DeckUpdateRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public List<DeckSummaryResponse> getDeckSummaries() {
        return deckService.getAllDecks();
    }

    @GetMapping("/{deckId}")
    public DeckResponse getDeckById(@PathVariable Long deckId) {
        return deckService.getDeckById(deckId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeckResponse> createDeck(
            @RequestPart("deck") String deckJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "imageIndexes", required = false) List<Integer> imageIndexes
    ) throws Exception {
        DeckRequest req = objectMapper.readValue(deckJson, DeckRequest.class);
        DeckResponse created = deckService.createDeck(req, images, imageIndexes);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{deckId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeckResponse> updateDeck(
            @PathVariable Long deckId,
            @RequestParam("deck") String deckJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "imageIndexes", required = false) List<Integer> imageIndexes
    ) throws Exception {
        DeckUpdateRequest req = objectMapper.readValue(deckJson, DeckUpdateRequest.class);
        DeckResponse updated = deckService.updateDeck(deckId, req, images, imageIndexes);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
        return ResponseEntity.noContent().build();
    }
}
