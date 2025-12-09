package com.news24h.news24hbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news24h.news24hbackend.dto.*;
import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NewsArticleRepository newsRepo;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String geminiUrl;

    public AiService(NewsArticleRepository newsRepo) {
        this.newsRepo = newsRepo;
    }

    public AiSummaryResponse summarize(AiSummaryRequest request) {
        String content = request.getContent();

        if (content == null && request.getArticleId() != null) {
            NewsArticle article = newsRepo.findById(request.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Article not found"));
            content = article.getContent() != null ? article.getContent() : article.getDescription();
        }

        if (content == null) {
            return AiSummaryResponse.builder()
                    .bullets(List.of("Không có nội dung để tóm tắt."))
                    .build();
        }

        String prompt = "Tóm tắt nội dung bài báo tiếng Việt dưới dạng 3 gạch đầu dòng ngắn gọn:\n\n"
                + content;

        List<String> bullets = callGeminiForBullets(prompt);
        return AiSummaryResponse.builder()
                .bullets(bullets)
                .build();
    }

    public AiChatResponse chat(AiChatRequest request) {
        String prompt = "Bạn là trợ lý ảo cho website tin tức 24h. Hãy trả lời ngắn gọn, dễ hiểu.\n\nUser: "
                + request.getMessage();

        String reply = callGeminiForText(prompt);
        return AiChatResponse.builder()
                .reply(reply)
                .build();
    }

    private List<String> callGeminiForBullets(String prompt) {
        try {
            String text = callGeminiForText(prompt + "\n\nĐầu ra: chỉ trả về 3 gạch đầu dòng '- ...'");
            List<String> result = new ArrayList<>();
            for (String line : text.split("\n")) {
                line = line.trim();
                if (line.startsWith("-")) {
                    result.add(line.substring(1).trim());
                }
            }
            if (result.isEmpty()) {
                result.add(text);
            }
            return result;
        } catch (Exception e) {
            return List.of("Không thể tóm tắt bằng AI tại thời điểm này.");
        }
    }

    private String callGeminiForText(String prompt) {
        try {
            // Request body theo format Gemini generative language API
            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> body = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String url = geminiUrl + "?key=" + apiKey;

            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);

            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                return candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText("");
            }
            return "Không nhận được phản hồi từ AI.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gọi AI.";
        }
    }
}

