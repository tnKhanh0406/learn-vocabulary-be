package com.prj.learnvocabularybe.dto.request;

public record NotificationSettingsRequest(
        Boolean enabled,
        String time   // format: "HH:mm", ví dụ "08:00"
) {}
