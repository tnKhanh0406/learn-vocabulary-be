package com.prj.learnvocabularybe.dto.request;

/**
 * Body gửi lên khi tạo hoặc cập nhật lịch nhắc học.
 *
 * @param time    Giờ nhắc, định dạng "HH:mm" (vd: "08:30"). Null khi chỉ muốn cập nhật enabled.
 * @param enabled Bật/tắt lịch này. Null khi chỉ muốn cập nhật time.
 */
public record StudyReminderRequest(
        String time,
        Boolean enabled
) {}
