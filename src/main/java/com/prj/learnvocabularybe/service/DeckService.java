package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.DeckRequest;
import com.prj.learnvocabularybe.dto.request.DeckUpdateRequest;
import com.prj.learnvocabularybe.dto.response.DeckResponse;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Khai báo các nghiệp vụ thao tác với bộ từ vựng (deck).
 */
public interface DeckService {
    /**
     * Lấy danh sách deck của người dùng hiện tại, có hỗ trợ tìm kiếm.
     */
    List<DeckSummaryResponse> getAllDecks(String q);

    /**
     * Lấy chi tiết một deck theo id nếu người dùng có quyền xem.
     */
    DeckResponse getDeckById(Long id);

    /**
     * Tạo deck mới kèm danh sách từ và ảnh minh hoạ.
     */
    DeckResponse createDeck(DeckRequest deckRequest, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception;

    /**
     * Cập nhật deck hiện có, bao gồm thông tin deck và danh sách từ.
     */
    DeckResponse updateDeck(Long id, DeckUpdateRequest request, List<MultipartFile> images, List<Integer> imageIndexes) throws Exception;

    /**
     * Xóa deck theo id.
     */
    void deleteDeck(Long id);

    /**
     * Lấy các deck của người dùng hiện tại chưa được gắn vào folder nào.
     */
    List<DeckSummaryResponse> getAllDecksNotInFolder();

    /**
     * Gán deck vào một folder.
     */
    DeckResponse addDeckToFolder(Long deckId, Long folderId);

    /**
     * Sao chép deck hiện có cho người dùng hiện tại.
     */
    DeckResponse copyDeck(Long sourceDeckId);

    /**
     * Tìm các deck public theo tên.
     */
    List<DeckSummaryResponse> searchPublicDecksByName(String q);

    /**
     * Lấy các deck public của một người dùng khác.
     */
    List<DeckSummaryResponse> getPublicDecksByUserId(Long userId);
}
