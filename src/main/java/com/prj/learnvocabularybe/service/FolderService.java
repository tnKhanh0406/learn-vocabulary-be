package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.FolderRequest;
import com.prj.learnvocabularybe.dto.response.FolderResponse;
import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;

import java.util.List;

public interface FolderService {
    List<FolderSummaryResponse> getAllFolders();
    FolderResponse getFolderById(Long folderId);
    FolderResponse createFolder(FolderRequest request);
    FolderResponse updateFolder(Long folderId, FolderRequest request);
    void deleteFolder(Long folderId);
}
