package com.prj.learnvocabularybe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

/**
 * Quản lý các endpoint thao tác với deck.
 */
@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;
    private final ObjectMapper objectMapper;

    /**
     * Lấy danh sách deck của người dùng hiện tại, có thể lọc theo từ khóa.
     */
    @GetMapping
    public List<DeckSummaryResponse> getDeckSummaries(@RequestParam(value = "q", required = false) String q) {
        return deckService.getAllDecks(q);
    }

    /**
     * Lấy chi tiết deck theo id.
     */
    @GetMapping("/{deckId}")
    public DeckResponse getDeckById(@PathVariable Long deckId) {
        return deckService.getDeckById(deckId);
    }

    /**
     * Tạo deck mới từ payload JSON và danh sách ảnh multipart.
     */
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

    /**
     * Cập nhật deck hiện có từ payload JSON và danh sách ảnh multipart.
     */
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

    /**
     * Xóa deck theo id.
     */
    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy các deck chưa được gắn vào folder nào.
     */
    @GetMapping("/no-folder")
    public List<DeckSummaryResponse> getDecksNotInFolder() {
        return deckService.getAllDecksNotInFolder();
    }

    /**
     * Gắn deck vào một folder cụ thể.
     */
    @PutMapping("/{deckId}/add-to-folder/{folderId}")
    public ResponseEntity<DeckResponse> addDeckToFolder(@PathVariable Long deckId,
                                                        @PathVariable Long folderId) {
        DeckResponse response = deckService.addDeckToFolder(deckId, folderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Sao chép deck hiện tại cho người dùng.
     */
    @PostMapping("/{deckId}/copy")
    public ResponseEntity<DeckResponse> copyDeck(@PathVariable Long deckId) {
        DeckResponse copied = deckService.copyDeck(deckId);
        return ResponseEntity.status(HttpStatus.CREATED).body(copied);
    }

    /**
     * Tìm deck public theo tên.
     */
    @GetMapping("/search")
    public List<DeckSummaryResponse> searchPublicDecksByName(@RequestParam("q") String q) {
        return deckService.searchPublicDecksByName(q);
    }

    /**
     * Lấy danh sách deck public của một người dùng khác.
     */
    @GetMapping("/user/{userId}")
    public List<DeckSummaryResponse> getPublicDecksByUserId(@PathVariable Long userId) {
        return deckService.getPublicDecksByUserId(userId);
    }
}
