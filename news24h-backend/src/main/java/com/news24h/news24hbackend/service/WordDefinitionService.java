package com.news24h.news24hbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news24h.news24hbackend.dto.WordDefinitionRequest;
import com.news24h.news24hbackend.dto.WordDefinitionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WordDefinitionService {

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Cache để tránh gọi API nhiều lần (30 phút)
    private final Map<String, WordDefinitionResponse> definitionCache = new ConcurrentHashMap<>();

    public WordDefinitionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public WordDefinitionResponse getDefinition(WordDefinitionRequest request) {
        try {
            String word = request.getWord().trim();
            String language = request.getLanguage() != null ? request.getLanguage() : "vi";

            // Validate
            if (word.length() < 2) {
                return new WordDefinitionResponse(false, word, null, null, null, "Từ không hợp lệ");
            }

            // Kiểm tra cache
            String cacheKey = word.toLowerCase() + "_" + language;
            if (definitionCache.containsKey(cacheKey)) {
                log.info("Cache hit for word: {}", word);
                return definitionCache.get(cacheKey);
            }

            // Gọi Gemini API
            String prompt = buildPrompt(word, language);
            String geminiResponse = callGeminiApi(prompt);

            // Parse response
            WordDefinitionResponse result = parseGeminiResponse(word, geminiResponse);

            // Lưu vào cache (30 phút)
            definitionCache.put(cacheKey, result);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            definitionCache.remove(cacheKey);
                        }
                    },
                    30 * 60 * 1000
            );

            return result;

        } catch (Exception e) {
            log.error("Error getting definition: ", e);
            return new WordDefinitionResponse(false, request.getWord(), null, null, null,
                    "Lỗi server khi xử lý yêu cầu");
        }
    }

    private String buildPrompt(String word, String language) {
        String lang = "vi".equals(language) ? "Tiếng Việt" : "Tiếng Anh";
        return String.format(
                "Cho tôi định nghĩa ngắn gọn của từ \"%s\" (%s).%n%n" +
                        "Trả lời theo format JSON sau (không có markdown, chỉ JSON):%n" +
                        "{%n" +
                        "  \"word\": \"%s\",%n" +
                        "  \"definition\": \"định nghĩa ngắn (tối đa 100 từ)\",%n" +
                        "  \"partOfSpeech\": \"loại từ (danh từ, động từ, tính từ, ...)\",%n" +
                        "  \"example\": \"ví dụ sử dụng từ này\"%n" +
                        "}%n%n" +
                        "Nếu không tìm thấy, trả về: {\"error\": \"Không tìm thấy định nghĩa\"}",
                word, lang, word
        );
    }

    private String callGeminiApi(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();

        parts.put("text", prompt);
        contents.put("parts", new Object[]{parts});
        requestBody.put("contents", new Object[]{contents});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        // Extract text từ response
        if (response != null && response.containsKey("candidates")) {
            var candidates = (java.util.List<?>) response.get("candidates");
            if (!candidates.isEmpty()) {
                var candidate = (Map<?, ?>) candidates.get(0);
                var content = (Map<?, ?>) candidate.get("content");
                var parts_response = (java.util.List<?>) content.get("parts");
                if (!parts_response.isEmpty()) {
                    var part = (Map<?, ?>) parts_response.get(0);
                    return (String) part.get("text");
                }
            }
        }

        throw new RuntimeException("Invalid Gemini API response");
    }

    private WordDefinitionResponse parseGeminiResponse(String word, String geminiText) {
        try {
            // Extract JSON từ response
            int startIdx = geminiText.indexOf('{');
            int endIdx = geminiText.lastIndexOf('}');

            if (startIdx == -1 || endIdx == -1) {
                return new WordDefinitionResponse(false, word, null, null, null,
                        "Không tìm thấy định nghĩa");
            }

            String jsonStr = geminiText.substring(startIdx, endIdx + 1);
            JsonNode jsonNode = objectMapper.readTree(jsonStr);

            // Kiểm tra lỗi
            if (jsonNode.has("error")) {
                return new WordDefinitionResponse(false, word, null, null, null,
                        jsonNode.get("error").asText());
            }

            // Parse thành công
            return new WordDefinitionResponse(
                    true,
                    jsonNode.get("word").asText(),
                    jsonNode.get("definition").asText(),
                    jsonNode.get("partOfSpeech").asText(),
                    jsonNode.get("example").asText(),
                    null
            );

        } catch (Exception e) {
            log.error("Error parsing Gemini response: ", e);
            return new WordDefinitionResponse(false, word, null, null, null,
                    "Không tìm thấy định nghĩa");
        }
    }
}
