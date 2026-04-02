package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.LoginRequest;
import com.prj.learnvocabularybe.dto.request.RegisterRequest;
import com.prj.learnvocabularybe.dto.response.AuthResponse;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.repository.UserRepository;
import com.prj.learnvocabularybe.service.AuthService;
import com.prj.learnvocabularybe.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));

        // saveAndFlush để lấy ID ngay lập tức trong cùng transaction
        UserEntity savedUser = userRepository.saveAndFlush(user);

        String token = jwtUtil.generateToken(savedUser.getUsername());
        return toAuthResponse(token, savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        UserEntity user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return toAuthResponse(token, user);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (request.username().trim().length() < 3 || request.username().trim().length() > 50) {
            throw new IllegalArgumentException("Tên đăng nhập phải từ 3 đến 50 ký tự");
        }
        if (!request.username().trim().matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (!request.email().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }

    private AuthResponse toAuthResponse(String token, UserEntity user) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }
}
