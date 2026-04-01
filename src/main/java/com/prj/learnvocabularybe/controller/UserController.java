package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserPublicResponse;
import com.prj.learnvocabularybe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search")
    public List<SearchUserResponse> searchUsersByUsername(String q) {
        return userService.searchUsersByUsername(q);
    }

    @GetMapping("/{userId}/public")
    public UserPublicResponse getPublicUserInfo(@PathVariable Long userId) {
        return userService.getPublicUserInfo(userId);
    }
}
