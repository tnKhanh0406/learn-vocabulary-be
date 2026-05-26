package com.prj.learnvocabularybe.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prj.learnvocabularybe.dto.request.ChangePasswordRequest;
import com.prj.learnvocabularybe.dto.request.UpdateProfileRequest;
import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserProfileResponse;
import com.prj.learnvocabularybe.dto.response.UserPublicResponse;
import com.prj.learnvocabularybe.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Cung cấp các endpoint thao tác với hồ sơ người dùng.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Tìm kiếm người dùng theo username.
     */
    @GetMapping("/search")
    public List<SearchUserResponse> searchUsersByUsername(@RequestParam String q) {
        return userService.searchUsersByUsername(q);
    }

    /**
     * Lấy hồ sơ của người dùng đang đăng nhập.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    /**
     * Cập nhật username hoặc email của người dùng hiện tại.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    /**
     * Đổi mật khẩu của người dùng hiện tại.
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload hoặc thay đổi ảnh đại diện.
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateAvatar(
            @RequestParam("avatar") MultipartFile avatar) throws Exception {
        return ResponseEntity.ok(userService.updateAvatar(avatar));
    }

    /**
     * Lấy thông tin public của một người dùng.
     */
    @GetMapping("/{userId}/public")
    public UserPublicResponse getPublicUserInfo(@PathVariable Long userId) {
        return userService.getPublicUserInfo(userId);
    }
}
