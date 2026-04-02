package com.prj.learnvocabularybe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.DeckUpdateRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.service.DeckService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/decks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public List<DeckSummaryResponse> getDeckSummaries(@RequestParam(value = "q", required = false) String q) {
        return deckService.getAllDecks(q);
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

    @GetMapping("/no-folder")
    public List<DeckSummaryResponse> getDecksNotInFolder() {
        return deckService.getAllDecksNotInFolder();
    }

    @PutMapping("/{deckId}/add-to-folder/{folderId}")
    public ResponseEntity<DeckResponse> addDeckToFolder(@PathVariable Long deckId,
                                                        @PathVariable Long folderId) {
        DeckResponse response = deckService.addDeckToFolder(deckId, folderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deckId}/copy")
    public ResponseEntity<DeckResponse> copyDeck(@PathVariable Long deckId) {
        DeckResponse copied = deckService.copyDeck(deckId);
        return ResponseEntity.status(HttpStatus.CREATED).body(copied);
    }

    @GetMapping("/search")
    public List<DeckSummaryResponse> searchPublicDecksByName(@RequestParam("q") String q) {
        return deckService.searchPublicDecksByName(q);
    }

    @GetMapping("/user/{userId}")
    public List<DeckSummaryResponse> getPublicDecksByUserId(@PathVariable Long userId) {
        return deckService.getPublicDecksByUserId(userId);
    }
}
