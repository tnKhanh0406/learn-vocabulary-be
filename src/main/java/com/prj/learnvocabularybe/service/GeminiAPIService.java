package com.prj.learnvocabularybe.service;

import com.prj.learnvocabularybe.dto.response.AiExplanationResponse;

public interface GeminiAPIService {
    // Lấy giải nghĩa AI (ưu tiên check Cache DB trước khi gọi API)
    AiExplanationResponse getExplanation(Long wordMeaningId, String word);
}