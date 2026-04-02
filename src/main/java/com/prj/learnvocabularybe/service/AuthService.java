package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.LoginRequest;
import com.prj.learnvocabularybe.dto.request.RegisterRequest;
import com.prj.learnvocabularybe.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
