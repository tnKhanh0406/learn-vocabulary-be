package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.FolderRequest;
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

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final DeckRepository deckRepository;

    @Override
    public List<FolderSummaryResponse> getAllFolders() {
        List<FolderEntity> folderEntities = folderRepository.findAllByUserId(SecurityUtil.getCurrentUser().getId());
        return folderEntities.stream()
                .map(FolderMapper::map)
                .toList();
    }

    @Override
    public FolderResponse createFolder(FolderRequest request) {
        FolderEntity folderEntity = new FolderEntity();
        folderEntity.setName(request.name());
        folderEntity.setDescription(request.description());
        folderEntity.setUser(SecurityUtil.getCurrentUser());
        folderRepository.save(folderEntity);

        return new FolderResponse(
                folderEntity.getId(),
                folderEntity.getName(),
                folderEntity.getDescription(),
                new ArrayList<>()
        );
    }

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

    @Override
    public void deleteFolder(Long folderId) {
        FolderEntity folderEntity = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + folderId));
        folderRepository.delete(folderEntity);
    }
}
