package com.prj.learnvocabularybe.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper; 
import com.prj.learnvocabularybe.dto.response.AiExplanationResponse;
import com.prj.learnvocabularybe.entity.DeckEntity;
import com.prj.learnvocabularybe.entity.DeckWordEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.entity.VocabularyEntity;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import com.prj.learnvocabularybe.repository.DeckRepository;
import com.prj.learnvocabularybe.repository.DeckWordRepository;
import com.prj.learnvocabularybe.repository.VocabularyRepository;
import com.prj.learnvocabularybe.repository.WordMeaningRepository;
import com.prj.learnvocabularybe.service.CloudinaryService;
import com.prj.learnvocabularybe.service.GeminiAPIService; 
import com.prj.learnvocabularybe.service.TranslateTtsService;
import com.prj.learnvocabularybe.util.SecurityUtil;

@Service
public class GeminiAPIServiceImpl implements GeminiAPIService {

    private static final Pattern TOPIC_PATTERN = Pattern.compile("(?:theo\\s+)?(?:chủ\\s*đề|chu\\s*de)\\s+(.+)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @Autowired
    private WordMeaningRepository wordMeaningRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private DeckWordRepository deckWordRepository;

    @Autowired
    private TranslateTtsService ttsService;

    @Autowired
    private CloudinaryService cloudinaryService;

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
            String topic = extractRequestedTopic(question);
            if (topic != null) {
                UserEntity currentUser = SecurityUtil.getCurrentUser();
                Optional<DeckEntity> existingDeck = deckRepository.findFirstByUser_IdAndTopicIgnoreCase(currentUser.getId(), topic);
                if (existingDeck.isPresent()) {
                    DeckEntity deck = existingDeck.get();
                    return "Mình đã tìm thấy bộ bài học theo chủ đề '" + deck.getTopic() + "' trong hệ thống. Bạn có thể học ngay với bộ bài ID " + deck.getId() + ".";
                }

                DeckEntity newDeck = createDeckByTopic(topic, currentUser);
                return "Mình đã tạo bộ bài học theo chủ đề '" + newDeck.getTopic() + "' và lưu vào hệ thống rồi. Bạn mở bộ bài ID " + newDeck.getId() + " để thêm từ hoặc bắt đầu học nhé.";
            }

            String prompt = "Bạn là trợ lý học tiếng Anh thân thiện. Hãy trả lời trực tiếp câu hỏi của người dùng bằng tiếng Việt, ngắn gọn, dễ hiểu, không trả về JSON, không dùng các nhãn như Nghĩa/Giải thích/Ví dụ. Câu hỏi: " + question;
            return callGeminiForText(prompt, false);
        } catch (Exception e) {
            System.out.println("Lỗi chat Gemini API: " + e.getMessage());
            e.printStackTrace();
            return "AI đang bận tạm thời. Bạn thử lại sau ít giây nhé.";
        }
    }

    private String extractRequestedTopic(String question) {
        if (question == null) {
            return null;
        }

        String normalized = question.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        String lowered = normalized.toLowerCase(Locale.ROOT);
        boolean asksForDeck = lowered.contains("bộ bài") || lowered.contains("bo bai") || lowered.contains("deck");
        boolean hasTopicKeyword = lowered.contains("chủ đề") || lowered.contains("chu de");
        if (!asksForDeck || !hasTopicKeyword) {
            return null;
        }

        Matcher matcher = TOPIC_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }

        String topic = matcher.group(1).trim();
        topic = topic.replaceAll("^[\\s:,.!?-]+|[\\s:,.!?-]+$", "");
        return topic.isEmpty() ? null : topic;
    }

    @Transactional
    private DeckEntity createDeckByTopic(String topic, UserEntity currentUser) {
        DeckEntity newDeck = new DeckEntity();
        newDeck.setName("Bộ bài chủ đề " + topic);
        newDeck.setDescription("Bộ bài được tạo tự động từ chat theo chủ đề " + topic);
        newDeck.setTopic(topic);
        newDeck.setIsPublic(false);
        newDeck.setUser(currentUser);
        newDeck.setCreatedBy(currentUser);
        newDeck.setIsGeneratedByAI(true);
        newDeck.setIsAiGenerated(1);
        newDeck = deckRepository.save(newDeck);

        List<TopicWord> generatedWords = generateTopicWords(topic);
        if (generatedWords.isEmpty()) {
            return newDeck;
        }

        Map<String, VocabularyEntity> vocabCache = new HashMap<>();
        List<WordMeaningEntity> meanings = new ArrayList<>();

        for (TopicWord item : generatedWords) {
            VocabularyEntity vocab = getOrCreateVocabulary(item.english(), vocabCache);

            WordMeaningEntity meaning = new WordMeaningEntity();
            meaning.setVocabulary(vocab);
            meaning.setUser(currentUser);
            meaning.setMeaning(item.vietnamese());
            meaning.setExplanation("Từ vựng theo chủ đề " + topic);
            meanings.add(meaning);
        }

        List<WordMeaningEntity> savedMeanings = wordMeaningRepository.saveAll(meanings);

        List<DeckWordEntity> deckWords = new ArrayList<>();
        for (WordMeaningEntity meaning : savedMeanings) {
            DeckWordEntity dw = new DeckWordEntity();
            dw.setDeck(newDeck);
            dw.setWordMeaning(meaning);
            deckWords.add(dw);
        }
        deckWordRepository.saveAll(deckWords);
        return newDeck;
    }

    private VocabularyEntity getOrCreateVocabulary(String english, Map<String, VocabularyEntity> vocabCache) {
        String normalized = english == null ? "" : english.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("english is required");
        }

        String key = normalized.toLowerCase(Locale.ROOT);
        VocabularyEntity cached = vocabCache.get(key);
        if (cached != null) {
            return cached;
        }

        VocabularyEntity vocab = vocabularyRepository.findByWordIgnoreCase(normalized)
                .orElseGet(() -> {
                    VocabularyEntity created = new VocabularyEntity();
                    created.setWord(normalized);
                    try {
                        return vocabularyRepository.save(created);
                    } catch (DataIntegrityViolationException ex) {
                        return vocabularyRepository.findByWordIgnoreCase(normalized)
                                .orElseThrow(() -> ex);
                    }
                });

        ensureAudioUrl(vocab, normalized);

        vocabCache.put(key, vocab);
        return vocab;
    }

    private void ensureAudioUrl(VocabularyEntity vocab, String englishWord) {
        if (vocab.getAudioUrl() != null && !vocab.getAudioUrl().isBlank()) {
            return;
        }

        try {
            byte[] mp3 = ttsService.synthesizeEnglishToMp3(englishWord);
            String audioPublicId = "vocab/" + sanitize(englishWord) + "_audio";
            String audioUrl = cloudinaryService.uploadAudioMp3(mp3, "flashcards/audio", audioPublicId);
            vocab.setAudioUrl(audioUrl);
            vocabularyRepository.save(vocab);
        } catch (Exception ex) {
            System.out.println("[WARN] Không tạo được audio cho từ '" + englishWord + "': " + ex.getMessage());
        }
    }

    private String sanitize(String raw) {
        return raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]+", "_");
    }

    private List<TopicWord> generateTopicWords(String topic) {
        String prompt = "Bạn là trợ lý tạo bộ từ vựng. Hãy trả về JSON array gồm đúng 10 phần tử cho chủ đề '"
                + topic
                + "'. Mỗi phần tử có 2 key: english, vietnamese. Chỉ trả về JSON thuần, không markdown, không giải thích.";

        try {
            String raw = callGeminiForText(prompt, true);
            ObjectMapper mapper = new ObjectMapper();
            TopicWord[] parsed = mapper.readValue(raw, TopicWord[].class);
            return sanitizeTopicWords(parsed, topic);
        } catch (Exception e) {
            return fallbackTopicWords(topic);
        }
    }

    private List<TopicWord> sanitizeTopicWords(TopicWord[] parsed, String topic) {
        if (parsed == null || parsed.length == 0) {
            return fallbackTopicWords(topic);
        }

        Map<String, TopicWord> unique = new LinkedHashMap<>();
        for (TopicWord item : parsed) {
            if (item == null) {
                continue;
            }

            String english = item.english() == null ? "" : item.english().trim();
            String vietnamese = item.vietnamese() == null ? "" : item.vietnamese().trim();
            if (english.isEmpty() || vietnamese.isEmpty()) {
                continue;
            }

            String key = english.toLowerCase(Locale.ROOT);
            unique.putIfAbsent(key, new TopicWord(english, vietnamese));
            if (unique.size() >= 10) {
                break;
            }
        }

        if (unique.isEmpty()) {
            return fallbackTopicWords(topic);
        }
        return new ArrayList<>(unique.values());
    }

    private List<TopicWord> fallbackTopicWords(String topic) {
        String lowerTopic = topic == null ? "" : topic.toLowerCase(Locale.ROOT);
        if (lowerTopic.contains("du lich") || lowerTopic.contains("du lịch") || lowerTopic.contains("travel")) {
            return List.of(
                    new TopicWord("passport", "hộ chiếu"),
                    new TopicWord("ticket", "vé"),
                    new TopicWord("hotel", "khách sạn"),
                    new TopicWord("luggage", "hành lý"),
                    new TopicWord("airport", "sân bay"),
                    new TopicWord("map", "bản đồ"),
                    new TopicWord("tour", "chuyến du lịch"),
                    new TopicWord("reservation", "đặt chỗ"),
                    new TopicWord("visa", "thị thực"),
                    new TopicWord("schedule", "lịch trình")
            );
        }

        return List.of(
                new TopicWord("topic", "chủ đề"),
                new TopicWord("lesson", "bài học"),
                new TopicWord("practice", "luyện tập"),
                new TopicWord("example", "ví dụ"),
                new TopicWord("answer", "câu trả lời"),
                new TopicWord("question", "câu hỏi"),
                new TopicWord("memory", "ghi nhớ"),
                new TopicWord("review", "ôn tập"),
                new TopicWord("improve", "cải thiện"),
                new TopicWord("progress", "tiến bộ")
        );
    }

    private record TopicWord(String english, String vietnamese) {
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
        RestTemplate restTemplate = createRestTemplate();

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
            System.out.println("[DEBUG] Calling Gemini API with URL: " + API_URL);
            System.out.println("[DEBUG] API Key is set: " + (geminiApiKey != null && !geminiApiKey.isEmpty()));
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

            System.out.println("[DEBUG] Gemini API Response Status: " + response.getStatusCode());
            
            ObjectMapper mapper = new ObjectMapper();
            var responseJson = mapper.readTree(response.getBody());
            String text = responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("");
            return normalizeGeminiText(text);
        } catch (Exception e) {
            System.out.println("[ERROR] First attempt failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            
            if (allowFallback) {
                try {
                    System.out.println("[DEBUG] Trying fallback API URL: " + FALLBACK_API_URL);
                    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                    ResponseEntity<String> fallbackResponse = restTemplate.exchange(FALLBACK_API_URL, HttpMethod.POST, entity, String.class);

                    System.out.println("[DEBUG] Fallback API Response Status: " + fallbackResponse.getStatusCode());
                    
                    ObjectMapper mapper = new ObjectMapper();
                    var responseJson = mapper.readTree(fallbackResponse.getBody());
                    String text = responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("");
                    return normalizeGeminiText(text);
                } catch (Exception fallbackError) {
                    System.out.println("[ERROR] Fallback also failed: " + fallbackError.getClass().getName() + " - " + fallbackError.getMessage());
                    fallbackError.printStackTrace();
                    throw new RuntimeException("Không gọi được Gemini API", fallbackError);
                }
            }
            throw new RuntimeException("Không gọi được Gemini API", e);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
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