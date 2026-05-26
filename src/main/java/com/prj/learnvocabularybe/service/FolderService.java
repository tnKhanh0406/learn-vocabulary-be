package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.AddDecksToFolderRequest;
import com.prj.learnvocabularybe.dto.request.FolderRequest;
import com.prj.learnvocabularybe.dto.response.FolderPublicResponse;
import com.prj.learnvocabularybe.dto.response.FolderResponse;
import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;

import java.util.List;

/**
 * Khai báo các nghiệp vụ quản lý folder chứa deck.
 */
public interface FolderService {
    /**
     * Lấy toàn bộ folder của người dùng hiện tại.
     */
    List<FolderSummaryResponse> getAllFolders();

    /**
     * Lấy chi tiết một folder theo id.
     */
    FolderResponse getFolderById(Long folderId);

    /**
     * Tạo folder mới.
     */
    FolderResponse createFolder(FolderRequest request);

    /**
     * Cập nhật tên và mô tả folder.
     */
    FolderResponse updateFolder(Long folderId, FolderRequest request);

    /**
     * Xóa folder theo id.
     */
    void deleteFolder(Long folderId);

    /**
     * Thêm nhiều deck vào folder.
     */
    FolderResponse addDecksToFolder(Long folderId, AddDecksToFolderRequest request);

    /**
     * Gỡ một deck khỏi folder.
     */
    FolderResponse removeDeckFromFolder(Long folderId, Long deckId);

    /**
     * Lấy thông tin public của folder để người khác xem.
     */
    FolderPublicResponse getFolderPublicById(Long folderId);
}
