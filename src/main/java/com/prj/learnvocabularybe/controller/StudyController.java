package com.prj.learnvocabularybe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prj.learnvocabularybe.dto.request.ChatRequest;
import com.prj.learnvocabularybe.dto.request.ReviewActionRequest;
import com.prj.learnvocabularybe.dto.response.AiExplanationResponse;
import com.prj.learnvocabularybe.dto.response.ChatResponse;
import com.prj.learnvocabularybe.dto.response.StudyDashboardResponse;
import com.prj.learnvocabularybe.dto.response.StudyWordResponse;
import com.prj.learnvocabularybe.service.GeminiAPIService;
import com.prj.learnvocabularybe.service.SpacedRepetitionService;
import com.prj.learnvocabularybe.service.StudyDashboardService;
import com.prj.learnvocabularybe.util.SecurityUtil;

/**
 * Gộp các endpoint liên quan đến học tập, AI và dashboard.
 */
@RestController
@RequestMapping("/api/study")
public class StudyController {

    @Autowired
    private SpacedRepetitionService spacedRepetitionService;

    @Autowired
    private GeminiAPIService geminiAPIService;

    @Autowired
    private StudyDashboardService studyDashboardService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Lấy danh sách từ cần ôn tập trong ngày hôm nay.
     */
    @GetMapping("/today")
    public ResponseEntity<List<StudyWordResponse>> getWordsToReviewToday(@RequestParam Long userId) {
        List<StudyWordResponse> words = spacedRepetitionService.getWordsToReviewToday(userId);
        return ResponseEntity.ok(words);
    }

    /**
     * Lưu kết quả đánh giá sau khi người dùng học xong một từ.
     */
    @PostMapping("/review")
    public ResponseEntity<String> submitReviewResult(
            @RequestParam Long userId,
            @RequestBody ReviewActionRequest request) {
        spacedRepetitionService.processUserReview(userId, request);
        return ResponseEntity.ok("Đã cập nhật tiến độ học tập thành công!");
    }

    /**
     * Lấy giải nghĩa AI cho một từ vựng cụ thể.
     */
    @GetMapping("/ai-explain/{wordMeaningId}")
    public ResponseEntity<AiExplanationResponse> getAiExplanation(
            @PathVariable Long wordMeaningId,
            @RequestParam String word) {
        AiExplanationResponse response = geminiAPIService.getExplanation(wordMeaningId, word);
        return ResponseEntity.ok(response);
    }

    /**
     * Chat tự do với AI.
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatWithAI(@RequestBody ChatRequest request) {
        String question = request.getContent();
        if (question == null || question.trim().isEmpty()) {
            ChatResponse errorResponse = new ChatResponse("Vui lòng nhập câu hỏi.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String answer = geminiAPIService.chat(question.trim());
        return ResponseEntity.ok(new ChatResponse(answer));
    }

    /**
     * Lấy tổng quan dashboard học tập.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<StudyDashboardResponse> getDashboard(
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        Long resolvedUserId = userId;
        if (resolvedUserId == null) {
            try {
                resolvedUserId = securityUtil.getCurrentUser().getId();
            } catch (Exception ignored) {
                resolvedUserId = 2L;
            }
        }
        return ResponseEntity.ok(studyDashboardService.getDashboard(resolvedUserId));
    }
}