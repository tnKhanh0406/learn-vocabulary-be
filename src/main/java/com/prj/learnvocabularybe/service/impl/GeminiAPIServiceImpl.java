package com.prj.learnvocabularybe.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
    private String geminiApiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private final String FALLBACK_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Override
    public AiExplanationResponse getExplanation(Long wordMeaningId, String word) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Nếu wordMeaningId = 0 (tức là gọi từ chat tự do), bỏ qua check DB/cache
        if (wordMeaningId == 0) {
            return callGeminiApi(word);
        }

        // ...phần còn lại giữ nguyên...
        WordMeaningEntity meaningEntity = wordMeaningRepository.findById(wordMeaningId)
                .orElseThrow(() -> new RuntimeException("Word Meaning not found"));

        if (meaningEntity.getAiExplanationCache() != null && !meaningEntity.getAiExplanationCache().isEmpty()) {
            try {
                AiExplanationResponse response = objectMapper.readValue(meaningEntity.getAiExplanationCache(), AiExplanationResponse.class);
                response.setIsFromCache(true);
                return response;
            } catch (Exception e) {
                System.out.println("Lỗi parse cache JSON: " + e.getMessage());
            }
        }

        AiExplanationResponse aiResponse = callGeminiApi(word);

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

    @Override
    public String chat(String question) {
        try {
            String prompt = "Bạn là trợ lý học tiếng Anh thân thiện. Hãy trả lời trực tiếp câu hỏi của người dùng bằng tiếng Việt, ngắn gọn, dễ hiểu, không trả về JSON, không dùng các nhãn như Nghĩa/Giải thích/Ví dụ. Câu hỏi: " + question;
            return callGeminiForText(prompt, false);
        } catch (Exception e) {
            System.out.println("Lỗi chat Gemini API: " + e.getMessage());
            return "AI đang bận tạm thời. Bạn thử lại sau ít giây nhé.";
        }
    }

    // Hàm phụ trợ gọi HTTP Request tới Google Gemini
    private AiExplanationResponse callGeminiApi(String word) {
        // Tạo prompt để AI trả về JSON
        String prompt = "Explain the meaning of the word '" + word + "' in Vietnamese. Provide a response in JSON format with keys: 'meaning' (Vietnamese meaning), 'explanation' (detailed explanation in Vietnamese), 'example' (an example sentence in English).";

        try {
            ObjectMapper mapper = new ObjectMapper();
            String text = callGeminiForText(prompt, true);

            // Gemini có thể bọc JSON bằng markdown code fence (```json ... ```)
            String cleanedText = text == null ? "" : text.trim();
            if (cleanedText.startsWith("```")) {
                int firstNewline = cleanedText.indexOf('\n');
                if (firstNewline >= 0) {
                    cleanedText = cleanedText.substring(firstNewline + 1);
                }
                if (cleanedText.endsWith("```")) {
                    cleanedText = cleanedText.substring(0, cleanedText.length() - 3).trim();
                }
            }

            // Parse text as JSON (vì prompt yêu cầu JSON)
            AiExplanationResponse aiResponse = mapper.readValue(cleanedText, AiExplanationResponse.class);

            return aiResponse;
        } catch (Exception e) {
            System.out.println("Lỗi gọi Gemini API: " + e.getMessage());
            // Fallback to mock
            AiExplanationResponse mockResponse = new AiExplanationResponse();
            mockResponse.setMeaning("Không thể lấy nghĩa từ AI");
            mockResponse.setExplanation("Lỗi kết nối API");
            mockResponse.setExample("Example: " + word);
            return mockResponse;
        }
    }

    private String callGeminiForText(String prompt, boolean allowFallback) {
        RestTemplate restTemplate = new RestTemplate();

        String requestBody = "{"
            + "\"contents\": [{"
            + "\"parts\": [{"
            + "\"text\": \"" + prompt.replace("\"", "\\\"") + "\""
            + "}]"
            + "}]"
            + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-goog-api-key", geminiApiKey);

        try {
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            var responseJson = mapper.readTree(response.getBody());
            String text = responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("");
            return normalizeGeminiText(text);
        } catch (Exception e) {
            if (allowFallback) {
                try {
                    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                    ResponseEntity<String> fallbackResponse = restTemplate.exchange(FALLBACK_API_URL, HttpMethod.POST, entity, String.class);

                    ObjectMapper mapper = new ObjectMapper();
                    var responseJson = mapper.readTree(fallbackResponse.getBody());
                    String text = responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("");
                    return normalizeGeminiText(text);
                } catch (Exception fallbackError) {
                    throw new RuntimeException("Không gọi được Gemini API", fallbackError);
                }
            }
            throw new RuntimeException("Không gọi được Gemini API", e);
        }
    }

    private String normalizeGeminiText(String text) {
        String cleanedText = text == null ? "" : text.trim();
        if (cleanedText.startsWith("```")) {
            int firstNewline = cleanedText.indexOf('\n');
            if (firstNewline >= 0) {
                cleanedText = cleanedText.substring(firstNewline + 1);
            }
            if (cleanedText.endsWith("```")) {
                cleanedText = cleanedText.substring(0, cleanedText.length() - 3).trim();
            }
        }
        return cleanedText;
    }
}