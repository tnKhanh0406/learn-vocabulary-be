package com.prj.learnvocabularybe.service.impl;

import com.prj.learnvocabularybe.dto.request.NotificationSettingsRequest;
import com.prj.learnvocabularybe.dto.response.NotificationResponse;
import com.prj.learnvocabularybe.dto.response.NotificationSettingsResponse;
import com.prj.learnvocabularybe.entity.NotificationEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.repository.NotificationRepository;
import com.prj.learnvocabularybe.repository.UserRepository;
import com.prj.learnvocabularybe.service.NotificationService;
import com.prj.learnvocabularybe.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Override
    public NotificationSettingsResponse getSettings() {
        UserEntity user = securityUtil.getCurrentUser();
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return toSettingsResponse(user, unreadCount);
    }

    @Override
    @Transactional
    public NotificationSettingsResponse updateSettings(NotificationSettingsRequest request) {
        UserEntity user = securityUtil.getCurrentUser();

        if (request.enabled() != null) {
            user.setNotificationEnabled(request.enabled());
        }

        if (request.time() != null) {
            validateTimeFormat(request.time());
            user.setNotificationTime(request.time());
        }

        userRepository.save(user);

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return toSettingsResponse(user, unreadCount);
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        UserEntity user = securityUtil.getCurrentUser();
        return notificationRepository
                .findTop20ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        UserEntity user = securityUtil.getCurrentUser();
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo"));

        // Chỉ cho phép đánh dấu thông báo của chính mình
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Không có quyền truy cập thông báo này");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        UserEntity user = securityUtil.getCurrentUser();
        notificationRepository.markAllAsRead(user.getId());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Kiểm tra định dạng giờ hợp lệ (HH:mm) */
    private void validateTimeFormat(String time) {
        if (!time.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException(
                    "Giờ không hợp lệ, định dạng phải là HH:mm (ví dụ: 08:00). Nhận được: " + time
            );
        }
    }

    /**
     * Chuyển UserEntity thành NotificationSettingsResponse.
     * Dùng giá trị mặc định an toàn nếu cột DB trả về null
     * (trường hợp migration chưa kịp backfill).
     */
    private NotificationSettingsResponse toSettingsResponse(UserEntity user, long unreadCount) {
        boolean enabled = user.getNotificationEnabled() != null && user.getNotificationEnabled();
        String time = user.getNotificationTime() != null ? user.getNotificationTime() : "08:00";
        return new NotificationSettingsResponse(enabled, time, unreadCount);
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getType() != null ? entity.getType().name() : null,
                entity.getTitle(),
                entity.getBody(),
                entity.getIsRead(),
                entity.getCreatedAt()
        );
    }
}
