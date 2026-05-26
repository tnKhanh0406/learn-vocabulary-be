package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.StudyReminderRequest;
import com.prj.learnvocabularybe.dto.response.StudyReminderResponse;

import java.util.List;

/**
 * Khai báo các nghiệp vụ quản lý lịch nhắc học.
 */
public interface StudyReminderService {

    /**
     * Lấy toàn bộ lịch nhắc của người dùng hiện tại.
     */
    List<StudyReminderResponse> getMyReminders();

    /**
     * Thêm một lịch nhắc mới.
     */
    StudyReminderResponse addReminder(StudyReminderRequest request);

    /**
     * Cập nhật giờ hoặc trạng thái bật/tắt của lịch nhắc.
     */
    StudyReminderResponse updateReminder(Long id, StudyReminderRequest request);

    /**
     * Xóa một lịch nhắc theo id.
     */
    void deleteReminder(Long id);
}
