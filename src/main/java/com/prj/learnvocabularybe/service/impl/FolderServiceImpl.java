package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.AddDecksToFolderRequest;
import com.prj.learnvocabularybe.dto.request.FolderRequest;
import com.prj.learnvocabularybe.dto.response.FolderPublicResponse;
import com.prj.learnvocabularybe.dto.response.FolderResponse;
import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;
import com.prj.learnvocabularybe.entity.FolderEntity;
import com.prj.learnvocabularybe.mapper.FolderMapper;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.FolderRepository;
import com.prj.learnvocabularybe.service.FolderService;
import com.prj.learnvocabularybe.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt nghiệp vụ quản lý folder chứa deck cho người dùng.
 */
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final DeckRepository deckRepository;
    private final SecurityUtil securityUtil;

        /**
         * Lấy tất cả folder của người dùng hiện tại.
         */
    @Override
    public List<FolderSummaryResponse> getAllFolders() {
        List<FolderEntity> folderEntities = folderRepository.findAllByUserId(securityUtil.getCurrentUser().getId());
        return folderEntities.stream()
                .map(FolderMapper::map)
                .toList();
    }

        /**
         * Lấy chi tiết folder kèm danh sách deck bên trong.
         */
    @Override
    public FolderResponse getFolderById(Long folderId) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        return new FolderResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getDescription(),
                deckRepository.findDeckSummariesByFolderId(folderId)
        );
    }

        /**
         * Tạo folder mới và gắn chủ sở hữu là user đang đăng nhập.
         */
    @Override
    public FolderResponse createFolder(FolderRequest request) {
        FolderEntity folderEntity = new FolderEntity();
        folderEntity.setName(request.name());
        folderEntity.setDescription(request.description());
        folderEntity.setUser(securityUtil.getCurrentUser());
        folderRepository.save(folderEntity);

        return new FolderResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getDescription(),
                new ArrayList<>()
        );
    }

        /**
         * Cập nhật tên và mô tả folder hiện có.
         */
    @Override
    public FolderResponse updateFolder(Long folderId, FolderRequest request) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        folderEntity.setName(request.name());
        folderEntity.setDescription(request.description());
        folderRepository.save(folderEntity);
        return new FolderResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getDescription(),
                deckRepository.findDeckSummariesByFolderId(folderId)
        );
    }

        /**
         * Xóa folder theo id.
         */
    @Override
    public void deleteFolder(Long folderId) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        folderRepository.delete(folderEntity);
    }

        /**
         * Thêm nhiều deck vào folder theo danh sách id từ request.
         */
    @Override
    public FolderResponse addDecksToFolder(Long folderId, AddDecksToFolderRequest request) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        deckRepository.addDecksToFolder(folderEntity.getId(), request.deckIds());
        return new FolderResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getDescription(),
                deckRepository.findDeckSummariesByFolderId(folderEntity.getId())
        );
    }

        /**
         * Gỡ một deck khỏi folder và trả về trạng thái mới của folder.
         */
    @Override
    public FolderResponse removeDeckFromFolder(Long folderId, Long deckId) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        deckRepository.removeDeckFromFolder(deckId);
        return new FolderResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getDescription(),
                deckRepository.findDeckSummariesByFolderId(folderEntity.getId())
        );
    }

        /**
         * Lấy thông tin folder public để người khác có thể xem.
         */
    @Override
    public FolderPublicResponse getFolderPublicById(Long folderId) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        return new FolderPublicResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getUser().getUsername(),
                folderEntity.getUser().getAvatarUrl(),
                deckRepository.findDeckSummariesPublicByFolderId(folderId)
        );
    }
}
