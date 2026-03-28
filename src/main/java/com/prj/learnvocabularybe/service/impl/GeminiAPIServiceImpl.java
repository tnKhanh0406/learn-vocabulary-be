package com.prj.learnvocabularybe.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper; 
import com.prj.learnvocabularybe.dto.response.AiExplanationResponse;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import com.prj.learnvocabularybe.repository.WordMeaningRepository;
import com.prj.learnvocabularybe.service.GeminiAPIService; 

@Service
public class GeminiAPIServiceImpl implements GeminiAPIService {

    @Autowired
    private WordMeaningRepository wordMeaningRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey="AIzaSyD5Z6BWFTmmVOAwK2k5T2qY0a9qj_bYMBM";

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    @Override
    public AiExplanationResponse getExplanation(Long wordMeaningId, String word) {
        // 1. Kiểm tra trong DB xem đã có Cache chưa
        WordMeaningEntity meaningEntity = wordMeaningRepository.findById(wordMeaningId)
                .orElseThrow(() -> new RuntimeException("Word Meaning not found"));

        ObjectMapper objectMapper = new ObjectMapper();

        if (meaningEntity.getAiExplanationCache() != null && !meaningEntity.getAiExplanationCache().isEmpty()) {
            try {
                // Parse chuỗi JSON lưu trong DB thành Object trả về luôn
                AiExplanationResponse response = objectMapper.readValue(meaningEntity.getAiExplanationCache(), AiExplanationResponse.class);
                response.setIsFromCache(true);
                return response;
            } catch (Exception e) {
                System.out.println("Lỗi parse cache JSON: " + e.getMessage());
            }
        }

        // 2. Nếu chưa có Cache, gọi API Gemini
        AiExplanationResponse aiResponse = callGeminiApi(word);

        // 3. Lưu kết quả vào Cache DB để dùng cho lần sau
        if (aiResponse != null) {
            try {
                String jsonToCache = objectMapper.writeValueAsString(aiResponse);
                meaningEntity.setAiExplanationCache(jsonToCache);
                wordMeaningRepository.save(meaningEntity);
                aiResponse.setIsFromCache(false);
            } catch (Exception e) {
                System.out.println("Lỗi lưu cache: " + e.getMessage());
            }
        }

        return aiResponse;
    }

    // Hàm phụ trợ gọi HTTP Request tới Google Gemini
    private AiExplanationResponse callGeminiApi(String word) {
        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + geminiApiKey;
        
        // Bạn sẽ cần cấu hình body request chuẩn theo document của Google Gemini tại đây
        // Trọng tâm là yêu cầu nó trả về JSON: {"meaning": "...", "explanation": "...", "example": "..."}
        
        // Đoạn này tôi đang làm giả lập dữ liệu trả về để code bạn không bị lỗi đỏ
        // Khi tích hợp thật, bạn bóc tách JSON từ response của RestTemplate
        AiExplanationResponse mockResponse = new AiExplanationResponse();
        mockResponse.setMeaning("Nghĩa giả lập của " + word);
        mockResponse.setExplanation("AI đang phân tích từ này...");
        mockResponse.setExample("This is an example for " + word);
        return mockResponse;
    }
}