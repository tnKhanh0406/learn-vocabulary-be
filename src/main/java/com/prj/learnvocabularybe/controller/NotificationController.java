package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.dto.request.NotificationSettingsRequest;
import com.prj.learnvocabularybe.dto.response.NotificationResponse;
import com.prj.learnvocabularybe.dto.response.NotificationSettingsResponse;
import com.prj.learnvocabularybe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Lấy cài đặt nhắc học (enabled + time + unreadCount)
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> getSettings() {
        return ResponseEntity.ok(notificationService.getSettings());
    }

    // Cập nhật cài đặt nhắc học
    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> updateSettings(
            @RequestBody NotificationSettingsRequest request) {
        return ResponseEntity.ok(notificationService.updateSettings(request));
    }

    // Lấy danh sách thông báo (20 gần nhất)
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    // Đánh dấu 1 thông báo đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // Đánh dấu tất cả đã đọc
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
