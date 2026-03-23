package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.dto.request.FolderRequest;
import com.prj.learnvocabularybe.dto.response.FolderResponse;
import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;
import com.prj.learnvocabularybe.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public List<FolderSummaryResponse> getAllFolders() {
        return folderService.getAllFolders();
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(@RequestBody FolderRequest request) {
        FolderResponse response = folderService.createFolder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(@PathVariable Long folderId,
                                                       @RequestBody FolderRequest request) {
        FolderResponse response = folderService.updateFolder(folderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }

}
