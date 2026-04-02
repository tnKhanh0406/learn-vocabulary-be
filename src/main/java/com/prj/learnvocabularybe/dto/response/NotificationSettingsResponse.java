package com.prj.learnvocabularybe.dto.response;

public record NotificationSettingsResponse(
        Boolean enabled,
        String time,        // "HH:mm"
        long unreadCount    // số thông báo chưa đọc
) {}
