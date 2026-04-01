package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;
import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserPublicResponse;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.FolderRepository;
import com.prj.learnvocabularybe.repository.UserRepository;
import com.prj.learnvocabularybe.service.UserService;
import com.prj.learnvocabularybe.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FolderRepository folderRepository;

    @Override
    public List<SearchUserResponse> searchUsersByUsername(String q) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return userRepository.searchUsersByUsername(q, userId);
    }

    @Override
    public UserPublicResponse getPublicUserInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        List<DeckSummaryResponse> publicDecks = deckRepository.searchPublicDecksByUserId(userId);
        List<FolderSummaryResponse> publicFolders = folderRepository.searchPublicFoldersByUserId(userId);
        return new UserPublicResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                publicDecks,
                publicFolders
        );
    }
}
