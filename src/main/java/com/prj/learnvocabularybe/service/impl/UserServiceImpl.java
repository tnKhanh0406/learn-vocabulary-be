package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
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

    @Override
    public List<SearchUserResponse> searchUsersByUsername(String q) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return userRepository.searchUsersByUsername(q, userId);
    }
}
