package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.NotificationSettingsRequest;
import com.prj.learnvocabularybe.dto.response.NotificationResponse;
import com.prj.learnvocabularybe.dto.response.NotificationSettingsResponse;

import java.util.List;

/**
 * Khai báo các nghiệp vụ thông báo và cài đặt nhắc học.
 */
public interface NotificationService {

    /**
     * Lấy cài đặt nhắc học của người dùng hiện tại.
     */
    NotificationSettingsResponse getSettings();

    /**
     * Cập nhật trạng thái bật/tắt và giờ nhắc học.
     */
    NotificationSettingsResponse updateSettings(NotificationSettingsRequest request);

    /**
     * Lấy danh sách thông báo gần nhất của người dùng.
     */
    List<NotificationResponse> getMyNotifications();

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    void markAsRead(Long notificationId);

    /**
     * Đánh dấu toàn bộ thông báo là đã đọc.
     */
    void markAllAsRead();
}
