package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.LoginRequest;
import com.prj.learnvocabularybe.dto.request.RegisterRequest;
import com.prj.learnvocabularybe.dto.response.AuthResponse;

/**
 * Khai báo các nghiệp vụ xác thực của hệ thống.
 */
public interface AuthService {
    /**
     * Đăng ký tài khoản mới và trả về thông tin đăng nhập.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Đăng nhập bằng username/password và trả về JWT.
     */
    AuthResponse login(LoginRequest request);
}
