package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.ChangePasswordRequest;
import com.prj.learnvocabularybe.dto.request.UpdateProfileRequest;
import com.prj.learnvocabularybe.dto.response.DeckSummaryResponse;
import com.prj.learnvocabularybe.dto.response.FolderSummaryResponse;
import com.prj.learnvocabularybe.dto.response.SearchUserResponse;
import com.prj.learnvocabularybe.dto.response.UserProfileResponse;
import com.prj.learnvocabularybe.dto.response.UserPublicResponse;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.FolderRepository;
import com.prj.learnvocabularybe.repository.UserRepository;
import com.prj.learnvocabularybe.service.CloudinaryService;
import com.prj.learnvocabularybe.service.UserService;
import com.prj.learnvocabularybe.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Cài đặt các nghiệp vụ liên quan đến hồ sơ người dùng, mật khẩu và avatar.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final DeckRepository deckRepository;
    private final FolderRepository folderRepository;

    /**
     * Tìm người dùng theo username, loại trừ chính người đang đăng nhập.
     */
    @Override
    public List<SearchUserResponse> searchUsersByUsername(String q) {
        Long userId = securityUtil.getCurrentUser().getId();
        return userRepository.searchUsersByUsername(q, userId);
    }

    /**
     * Lấy hồ sơ của người dùng hiện tại.
     */
    @Override
    public UserProfileResponse getMyProfile() {
        UserEntity user = securityUtil.getCurrentUser();
        return buildProfileResponse(user);
    }

    /**
     * Cập nhật username và email nếu người dùng cung cấp giá trị mới hợp lệ.
     */
    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        UserEntity user = securityUtil.getCurrentUser();

        String newUsername = request.username() == null ? null : request.username().trim();
        String newEmail = request.email() == null ? null : request.email().trim().toLowerCase();

        // Validate và cập nhật username nếu thay đổi
        if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(user.getUsername())) {
            if (newUsername.length() < 3 || newUsername.length() > 50) {
                throw new IllegalArgumentException("Tên đăng nhập phải từ 3 đến 50 ký tự");
            }
            if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
                throw new IllegalArgumentException("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới");
            }
            if (userRepository.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
            }
            user.setUsername(newUsername);
        }

        // Validate và cập nhật email nếu thay đổi
        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
            if (!newEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new IllegalArgumentException("Email không hợp lệ");
            }
            if (userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
            user.setEmail(newEmail);
        }

        userRepository.save(user);
        return buildProfileResponse(user);
    }

    /**
     * Đổi mật khẩu sau khi xác minh mật khẩu cũ và kiểm tra độ dài mật khẩu mới.
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (request.oldPassword() == null || request.oldPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu cũ không được để trống");
        }
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (request.oldPassword().equals(request.newPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        UserEntity user = securityUtil.getCurrentUser();

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /**
     * Upload ảnh đại diện mới lên Cloudinary và lưu URL vào hồ sơ người dùng.
     */
    @Override
    @Transactional
    public UserProfileResponse updateAvatar(MultipartFile avatar) throws Exception {
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh đại diện");
        }

        // Kiểm tra định dạng file
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là ảnh (jpg, png, webp...)");
        }

        UserEntity user = securityUtil.getCurrentUser();

        // Upload lên Cloudinary, đặt tên theo userId để overwrite ảnh cũ
        String avatarUrl = cloudinaryService.uploadImage(
                avatar,
                "avatars",
                "avatar_" + user.getId()
        );

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return buildProfileResponse(user);
    }

    /**
     * Lấy thông tin public của người dùng kèm deck và folder public.
     */
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

    // Tạo UserProfileResponse từ entity + đếm deck/folder
    private UserProfileResponse buildProfileResponse(UserEntity user) {
        long deckCount = userRepository.countDecksByUserId(user.getId());
        long folderCount = userRepository.countFoldersByUserId(user.getId());
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                deckCount,
                folderCount
        );
    }
}
