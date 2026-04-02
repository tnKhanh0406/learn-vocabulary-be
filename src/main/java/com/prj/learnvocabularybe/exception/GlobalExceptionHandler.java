package com.prj.learnvocabularybe.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Xử lý tập trung tất cả exception trong ứng dụng.
 * Mỗi handler trả JSON { "message": "..." } thay vì HTML mặc định của Spring.
 *
 * Thứ tự ưu tiên (từ cụ thể → tổng quát):
 *   IllegalArgumentException → 400
 *   IllegalStateException    → 404
 *   SecurityException        → 403
 *   RuntimeException         → 401 (dùng cho auth errors từ SecurityUtil)
 *   HttpMessageNotReadable   → 400 (request body sai format JSON)
 *   Exception (fallback)     → 500
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 400 Bad Request ──────────────────────────────────────────────────────

    /** Lỗi validation input: dữ liệu không hợp lệ (vd: format giờ sai) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Request body không đúng JSON hoặc thiếu field bắt buộc */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return response(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ");
    }

    // ─── 401 Unauthorized ─────────────────────────────────────────────────────

    /**
     * Lỗi xác thực: user chưa đăng nhập hoặc token hết hạn.
     * SecurityUtil.getCurrentUser() ném RuntimeException trong các trường hợp này.
     * Dùng RuntimeException thay vì tạo custom exception để tránh thay đổi nhiều code.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(RuntimeException ex) {
        String msg = ex.getMessage();
        // Chỉ xử lý auth errors — các RuntimeException khác để fallback tới handler dưới
        if (msg != null && (msg.contains("chưa đăng nhập") || msg.contains("Không tìm thấy người dùng"))) {
            log.warn("Auth error: {}", msg);
            return response(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại");
        }
        // RuntimeException không phải auth error → log đầy đủ và trả 500
        log.error("RuntimeException không xác định: {}", msg, ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
    }

    // ─── 403 Forbidden ────────────────────────────────────────────────────────

    /** Truy cập tài nguyên không thuộc về mình */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return response(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ─── 404 Not Found ────────────────────────────────────────────────────────

    /** Không tìm thấy tài nguyên (deck, folder, notification, ...) */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalStateException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 500 Internal Server Error (fallback) ─────────────────────────────────

    /**
     * Bắt tất cả exception còn lại.
     * LOG ĐẦY ĐỦ STACK TRACE để debug — đây là lý do các 500 trước đây không rõ nguyên nhân.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Lỗi không xác định [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
