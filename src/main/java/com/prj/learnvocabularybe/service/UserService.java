package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.ChangePasswordRequest;
import com.prj.learnvocabularybe.dto.request.UpdateProfileRequest;
import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    // Tìm kiếm người dùng theo username
    List<SearchUserResponse> searchUsersByUsername(String q);

    // Lấy hồ sơ của user đang đăng nhập
    UserProfileResponse getMyProfile();

    // Cập nhật username / email
    UserProfileResponse updateProfile(UpdateProfileRequest request);

    // Đổi mật khẩu (xác minh mật khẩu cũ trước)
    void changePassword(ChangePasswordRequest request);

    // Upload ảnh đại diện lên Cloudinary
    UserProfileResponse updateAvatar(MultipartFile avatar) throws Exception;
}
