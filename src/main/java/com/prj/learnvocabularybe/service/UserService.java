package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;

import java.util.List;

public interface UserService {
    List<SearchUserResponse> searchUsersByUsername(String q);
}
