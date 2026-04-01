package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserPublicResponse;

import java.util.List;

public interface UserService {
    List<SearchUserResponse> searchUsersByUsername(String q);
    UserPublicResponse getPublicUserInfo(Long userId);
}
