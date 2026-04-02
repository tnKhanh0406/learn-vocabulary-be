package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.NotificationSettingsRequest;
import com.prj.learnvocabularybe.dto.response.NotificationResponse;
import com.prj.learnvocabularybe.dto.response.NotificationSettingsResponse;

import java.util.List;

public interface NotificationService {

    // Lấy cài đặt nhắc học của user hiện tại
    NotificationSettingsResponse getSettings();

    // Cập nhật cài đặt nhắc học (bật/tắt + giờ)
    NotificationSettingsResponse updateSettings(NotificationSettingsRequest request);

    // Lấy danh sách thông báo (20 gần nhất)
    List<NotificationResponse> getMyNotifications();

    // Đánh dấu 1 thông báo là đã đọc
    void markAsRead(Long notificationId);

    // Đánh dấu tất cả là đã đọc
    void markAllAsRead();
}
