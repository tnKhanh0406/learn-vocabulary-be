package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.ChangePasswordRequest;
import com.prj.learnvocabularybe.dto.request.UpdateProfileRequest;
import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserProfileResponse;
import com.prj.learnvocabularybe.dto.response.UserPublicResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Khai báo các nghiệp vụ liên quan đến thông tin người dùng.
 */
public interface UserService {

    /**
     * Tìm kiếm người dùng theo username, loại trừ chính người đang đăng nhập.
     */
    List<SearchUserResponse> searchUsersByUsername(String q);

    /**
     * Lấy hồ sơ của người dùng hiện tại.
     */
    UserProfileResponse getMyProfile();

    /**
     * Cập nhật username hoặc email của người dùng hiện tại.
     */
    UserProfileResponse updateProfile(UpdateProfileRequest request);

    /**
     * Đổi mật khẩu sau khi xác minh mật khẩu cũ.
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Upload ảnh đại diện mới lên Cloudinary và cập nhật hồ sơ.
     */
    UserProfileResponse updateAvatar(MultipartFile avatar) throws Exception;

    /**
     * Lấy thông tin public của một người dùng để hiển thị profile công khai.
     */
    UserPublicResponse getPublicUserInfo(Long userId);
}
