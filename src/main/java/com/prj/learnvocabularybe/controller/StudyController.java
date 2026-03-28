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

import com.prj.learnvocabularybe.dto.request.ReviewActionRequest;
import com.prj.learnvocabularybe.dto.response.AiExplanationResponse;
import com.prj.learnvocabularybe.dto.response.StudyWordResponse;
import com.prj.learnvocabularybe.service.GeminiAPIService;
import com.prj.learnvocabularybe.service.SpacedRepetitionService;

@RestController
@RequestMapping("/api/study")
// Thêm @CrossOrigin("*") nếu bạn bị lỗi CORS khi test với Frontend React/Vue
public class StudyController {

    @Autowired
    private SpacedRepetitionService spacedRepetitionService;

    @Autowired
    private GeminiAPIService geminiAPIService;

    // 1. API Lấy danh sách từ cần học hôm nay
    // Ví dụ gọi: GET /api/study/today?userId=1
    @GetMapping("/today")
    public ResponseEntity<List<StudyWordResponse>> getWordsToReviewToday(@RequestParam Long userId) {
        // Trong thực tế, userId thường được lấy từ Token bảo mật (JWT), 
        // nhưng tạm thời truyền qua tham số để test cho dễ.
        List<StudyWordResponse> words = spacedRepetitionService.getWordsToReviewToday(userId);
        return ResponseEntity.ok(words);
    }

    // 2. API Lưu kết quả sau khi người dùng bấm nút đánh giá (Quên, Nhớ...)
    // Ví dụ gọi: POST /api/study/review?userId=1
    @PostMapping("/review")
    public ResponseEntity<String> submitReviewResult(
            @RequestParam Long userId,
            @RequestBody ReviewActionRequest request) {
        
        spacedRepetitionService.processUserReview(userId, request);
        return ResponseEntity.ok("Đã cập nhật tiến độ học tập thành công!");
    }

    // 3. API Gọi Trợ lý AI giải nghĩa từ vựng
    // Ví dụ gọi: GET /api/study/ai-explain/15?word=apple
    @GetMapping("/ai-explain/{wordMeaningId}")
    public ResponseEntity<AiExplanationResponse> getAiExplanation(
            @PathVariable Long wordMeaningId,
            @RequestParam String word) {
        
        AiExplanationResponse response = geminiAPIService.getExplanation(wordMeaningId, word);
        return ResponseEntity.ok(response);
    }
}