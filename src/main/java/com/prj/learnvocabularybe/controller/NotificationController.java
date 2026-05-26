package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.dto.request.NotificationSettingsRequest;
import com.prj.learnvocabularybe.dto.response.NotificationResponse;
import com.prj.learnvocabularybe.dto.response.NotificationSettingsResponse;
import com.prj.learnvocabularybe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý API cho cài đặt thông báo và danh sách thông báo của người dùng.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lấy cài đặt nhắc học hiện tại.
     */
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> getSettings() {
        return ResponseEntity.ok(notificationService.getSettings());
    }

    /**
     * Cập nhật trạng thái bật/tắt và giờ nhắc học.
     */
    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> updateSettings(
            @RequestBody NotificationSettingsRequest request) {
        return ResponseEntity.ok(notificationService.updateSettings(request));
    }

    /**
     * Lấy danh sách thông báo gần nhất.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc.
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
