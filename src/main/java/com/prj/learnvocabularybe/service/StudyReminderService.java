package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.request.StudyReminderRequest;
import com.prj.learnvocabularybe.dto.response.StudyReminderResponse;

import java.util.List;

public interface StudyReminderService {

    /** Lấy tất cả lịch nhắc của user hiện tại */
    List<StudyReminderResponse> getMyReminders();

    /** Thêm lịch nhắc mới */
    StudyReminderResponse addReminder(StudyReminderRequest request);

    /** Cập nhật giờ hoặc trạng thái bật/tắt của lịch */
    StudyReminderResponse updateReminder(Long id, StudyReminderRequest request);

    /** Xóa lịch nhắc */
    void deleteReminder(Long id);
}
