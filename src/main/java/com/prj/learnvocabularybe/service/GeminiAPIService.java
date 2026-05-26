package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.response.AiExplanationResponse;

/**
 * Khai báo các nghiệp vụ liên quan tới Gemini AI.
 */
public interface GeminiAPIService {

    /**
     * Lấy giải thích AI cho một từ, ưu tiên cache trong database trước.
     */
    AiExplanationResponse getExplanation(Long wordMeaningId, String word);

    /**
     * Chat tự do và trả về câu trả lời dạng text.
     */
    String chat(String question);
}