package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.dto.request.StudyReminderRequest;
import com.prj.learnvocabularybe.dto.response.StudyReminderResponse;
import com.prj.learnvocabularybe.service.StudyReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD lịch nhắc học.
 *
 * GET    /api/reminders          — lấy danh sách lịch của user hiện tại
 * POST   /api/reminders          — thêm lịch mới
 * PUT    /api/reminders/{id}     — sửa giờ hoặc bật/tắt một lịch
 * DELETE /api/reminders/{id}     — xóa lịch
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class StudyReminderController {

    private final StudyReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<StudyReminderResponse>> getMyReminders() {
        return ResponseEntity.ok(reminderService.getMyReminders());
    }

    @PostMapping
    public ResponseEntity<StudyReminderResponse> addReminder(
            @RequestBody StudyReminderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reminderService.addReminder(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyReminderResponse> updateReminder(
            @PathVariable Long id,
            @RequestBody StudyReminderRequest request) {
        return ResponseEntity.ok(reminderService.updateReminder(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }
}
