package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.StudyReminderRequest;
import com.prj.learnvocabularybe.dto.response.StudyReminderResponse;
import com.prj.learnvocabularybe.entity.StudyReminderEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.repository.StudyReminderRepository;
import com.prj.learnvocabularybe.service.StudyReminderService;
import com.prj.learnvocabularybe.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt nghiệp vụ quản lý lịch nhắc học của người dùng.
 */
@Service
@RequiredArgsConstructor
public class StudyReminderServiceImpl implements StudyReminderService {

    /** Giới hạn số lịch nhắc tối đa mỗi user */
    private static final int MAX_REMINDERS = 10;

    private final StudyReminderRepository reminderRepository;
    private final SecurityUtil securityUtil;

    /**
     * Lấy toàn bộ lịch nhắc của người dùng hiện tại.
     */
    @Override
    public List<StudyReminderResponse> getMyReminders() {
        UserEntity user = securityUtil.getCurrentUser();
        return reminderRepository
                .findByUserIdOrderByTimeAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Thêm lịch nhắc mới nếu chưa vượt quá giới hạn và không trùng giờ.
     */
    @Override
    @Transactional
    public StudyReminderResponse addReminder(StudyReminderRequest request) {
        UserEntity user = securityUtil.getCurrentUser();

        validateTime(request.time());

        if (reminderRepository.countByUserId(user.getId()) >= MAX_REMINDERS) {
            throw new IllegalArgumentException("Tối đa " + MAX_REMINDERS + " lịch nhắc học");
        }

        if (reminderRepository.existsByUserIdAndTime(user.getId(), request.time())) {
            throw new IllegalArgumentException("Đã có lịch nhắc lúc " + request.time());
        }

        StudyReminderEntity entity = new StudyReminderEntity();
        entity.setUser(user);
        entity.setTime(request.time());
        // Mặc định bật khi mới tạo, trừ khi client gửi enabled = false
        entity.setEnabled(request.enabled() == null || request.enabled());

        return toResponse(reminderRepository.save(entity));
    }

    /**
     * Cập nhật giờ nhắc hoặc trạng thái bật/tắt của lịch.
     */
    @Override
    @Transactional
    public StudyReminderResponse updateReminder(Long id, StudyReminderRequest request) {
        UserEntity user = securityUtil.getCurrentUser();
        StudyReminderEntity entity = findAndCheckOwner(id, user);

        if (request.time() != null) {
            validateTime(request.time());

            boolean timeChanged = !request.time().equals(entity.getTime());
            if (timeChanged && reminderRepository.existsByUserIdAndTime(user.getId(), request.time())) {
                throw new IllegalArgumentException("Đã có lịch nhắc lúc " + request.time());
            }
            entity.setTime(request.time());
        }

        if (request.enabled() != null) {
            entity.setEnabled(request.enabled());
        }

        return toResponse(reminderRepository.save(entity));
    }

    /**
     * Xóa lịch nhắc theo id sau khi kiểm tra quyền sở hữu.
     */
    @Override
    @Transactional
    public void deleteReminder(Long id) {
        UserEntity user = securityUtil.getCurrentUser();
        StudyReminderEntity entity = findAndCheckOwner(id, user);
        reminderRepository.delete(entity);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Tìm lịch nhắc và kiểm tra quyền sở hữu của user hiện tại.
     */
    private StudyReminderEntity findAndCheckOwner(Long id, UserEntity currentUser) {
        StudyReminderEntity entity = reminderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch nhắc"));

        if (!entity.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Không có quyền chỉnh sửa lịch nhắc này");
        }

        return entity;
    }

    /**
     * Kiểm tra định dạng "HH:mm".
     * HH: 00–23, mm: 00–59.
     */
    private void validateTime(String time) {
        if (time == null || !time.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException(
                    "Định dạng giờ không hợp lệ. Phải là HH:mm (ví dụ: 08:30)"
            );
        }
    }

    private StudyReminderResponse toResponse(StudyReminderEntity entity) {
        return new StudyReminderResponse(
                entity.getId(),
                entity.getTime(),
                entity.getEnabled(),
                entity.getCreatedAt()
        );
    }
}
