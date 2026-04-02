package com.prj.learnvocabularybe.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search")
    public List<SearchUserResponse> searchUsersByUsername(String q) {
        return userService.searchUsersByUsername(q);
    }
}
