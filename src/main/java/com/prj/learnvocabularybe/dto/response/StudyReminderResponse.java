package com.prj.learnvocabularybe.dto.response;

import java.time.LocalDateTime;

/**
 * Thông tin 1 lịch nhắc học trả về cho client.
 *
 * @param id        ID trong database
 * @param time      Giờ nhắc, định dạng "HH:mm"
 * @param enabled   Lịch này có đang bật không
 * @param createdAt Thời điểm tạo
 */
public record StudyReminderResponse(
        Long id,
        String time,
        Boolean enabled,
        LocalDateTime createdAt
) {}
