package com.prj.learnvocabularybe.service;

import java.util.List;

import com.prj.learnvocabularybe.dto.request.ReviewActionRequest;
import com.prj.learnvocabularybe.dto.response.StudyWordResponse;

public interface SpacedRepetitionService {
    // Xử lý khi người dùng học xong 1 từ (chấm điểm 1-4)
    /**
     * Ghi nhận một lần ôn tập của người dùng và cập nhật tiến độ học.
     */
    void processUserReview(Long userId, ReviewActionRequest request);
    
    // Lấy danh sách các từ cần học hôm nay
    /**
     * Lấy danh sách từ cần ôn tập trong ngày hôm nay.
     */
    List<StudyWordResponse> getWordsToReviewToday(Long userId);
}