package com.prj.learnvocabularybe.util;

import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    /**
     * Lấy UserEntity của người dùng đang đăng nhập từ JWT token trong SecurityContext.
     *
     * Ném RuntimeException với message cụ thể để GlobalExceptionHandler có thể
     * phân biệt lỗi auth (→ 401) khỏi lỗi hệ thống khác (→ 500).
     */
    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("Truy cập API không có token hợp lệ");
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    // Trường hợp hiếm: token hợp lệ nhưng user đã bị xóa khỏi DB
                    log.warn("Token hợp lệ nhưng không tìm thấy user trong DB: {}", username);
                    return new RuntimeException("Không tìm thấy người dùng: " + username);
                });
    }
}
