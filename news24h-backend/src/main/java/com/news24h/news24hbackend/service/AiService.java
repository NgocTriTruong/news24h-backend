package com.news24h.news24hbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news24h.news24hbackend.dto.*;
import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NewsArticleRepository newsRepo;
    private final CacheManager cacheManager;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String geminiUrl;

    public AiService(NewsArticleRepository newsRepo, CacheManager cacheManager) {
        this.newsRepo = newsRepo;
        this.cacheManager = cacheManager;
    }

    public AiSummaryResponse summarize(AiSummaryRequest request) {
        try {
            String raw = request.getContent();

            String articleId = request.getArticleId();
            if ((raw == null || raw.isBlank()) && articleId != null) {
                // Cache theo articleId để 1 bài gọi 1 lần
                Cache cache = cacheManager.getCache("aiSummary");
                if (cache != null) {
                    AiSummaryResponse cached = cache.get(articleId, AiSummaryResponse.class);
                    if (cached != null) return cached;
                }

                NewsArticle article = newsRepo.findById(articleId)
                        .orElseThrow(() -> new RuntimeException("Article not found"));
                raw = article.getContent() != null ? article.getContent() : article.getDescription();
            }

            if (raw == null || raw.isBlank()) {
                return AiSummaryResponse.builder()
                        .bullets(List.of("Không có nội dung để tóm tắt."))
                        .build();
            }

            String text = htmlToText(raw);
            text = shrink(text, 16000); // chống prompt quá dài (tạm thời)

            String systemInstruction =
                    "Bạn là biên tập viên tin tức tiếng Việt. Hãy tóm tắt chính xác, không bịa thêm.";
            String userPrompt =
                    """
                        Hãy tóm tắt nội dung bài báo dưới đây bằng tiếng Việt.
                        
                        Yêu cầu:
                        - Tóm tắt thành 4–5 gạch đầu dòng (nếu nội dung ngắn thì có thể ít hơn, nhưng tối đa 5).
                        - Mỗi gạch là 1 câu hoàn chỉnh, rõ chủ thể, rõ hành động.
                        - Không suy diễn, không bịa thêm thông tin ngoài bài viết.
                        - Không dùng từ chung chung như “một số”, “nhiều người”, nếu bài có thông tin cụ thể.
                        - BẮT BUỘC mỗi gạch đầu dòng bắt đầu bằng "- ".
                        - Không viết đoạn văn, chỉ viết gạch đầu dòng.
                        
                        Nội dung bài viết:
                        """ + text;

            String out = callGemini(systemInstruction, buildContentsSingleTurn(userPrompt));

            List<String> bullets = parseBullets(out);
            AiSummaryResponse resp = AiSummaryResponse.builder().bullets(bullets).build();

            if (articleId != null) {
                Cache cache = cacheManager.getCache("aiSummary");
                if (cache != null) cache.put(articleId, resp);
            } else {
                // cache theo hash content (tuỳ chọn)
                Cache cache = cacheManager.getCache("aiSummary");
                if (cache != null) cache.put(hash(text), resp);
            }

            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            return AiSummaryResponse.builder()
                    .bullets(List.of("Không thể tóm tắt bằng AI tại thời điểm này."))
                    .build();
        }
    }

    public AiChatResponse chat(AiChatRequest request) {
        try {
            String msg = request.getMessage();
            if (msg == null || msg.isBlank()) {
                return AiChatResponse.builder().reply("Bạn muốn hỏi gì về tin tức hôm nay?").build();
            }

            // LẤY DỮ LIỆU TIN TỨC MỚI NHẤT TỪ DB
            List<NewsArticle> latestNews =
                    newsRepo.findTop20ByOrderByPublishedAtDesc();

            StringBuilder newsContext = new StringBuilder();
            for (NewsArticle n : latestNews) {
                newsContext.append("- ")
                        .append(n.getTitle())
                        .append(" (")
                        .append(n.getCategory())
                        .append(")\n");
            }

            String systemInstruction =
                    "Bạn là trợ lý ảo cho website tin tức 24h. Trả lời ngắn gọn, dễ hiểu, đúng trọng tâm. " +
                            "Nếu không đủ thông tin, hãy hỏi lại 1 câu để làm rõ.";

            List<Map<String, Object>> contents = new ArrayList<>();

            // Lịch sử chat (FE gửi lên)
            if (request.getHistory() != null) {
                for (AiChatMessage m : request.getHistory()) {
                    if (m == null || m.getText() == null) continue;
                    String role = normalizeRole(m.getRole());
                    contents.add(Map.of(
                            "role", role,
                            "parts", List.of(Map.of("text", m.getText()))
                    ));
                }
            }

            // Turn mới nhất
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", msg))
            ));

            String out = callGemini(systemInstruction, contents);

            return AiChatResponse.builder().reply(out).build();

        } catch (Exception e) {
            e.printStackTrace();
            return AiChatResponse.builder()
                    .reply("Xin lỗi, hiện tại tôi không trả lời được. Bạn thử lại sau nhé.")
                    .build();
        }
    }

    // ===== Helpers =====

    private String callGemini(String systemInstruction, List<Map<String, Object>> contents) throws Exception {
        Map<String, Object> body = new HashMap<>();
        // REST docs dùng system_instruction (snake_case) :contentReference[oaicite:3]{index=3}
        body.put("system_instruction", Map.of(
                "parts", List.of(Map.of("text", systemInstruction))
        ));
        body.put("contents", contents);

        // generationConfig: bạn có thể tune để rẻ hơn/ổn định hơn
        body.put("generationConfig", Map.of(
                "temperature", 0.2,
                "topP", 0.9,
                "maxOutputTokens", 512,
                // nếu muốn tắt "thinking" (tuỳ model) có thể thử:
                // "thinkingConfig", Map.of("thinkingBudget", 0)
                "candidateCount", 1
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = geminiUrl + "?key=" + apiKey;
        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return "Không nhận được phản hồi từ AI.";
        }

        JsonNode root = objectMapper.readTree(resp.getBody());
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            StringBuilder sb = new StringBuilder();
            if (parts.isArray()) {
                for (JsonNode p : parts) {
                    sb.append(p.path("text").asText(""));
                }
            }

            return sb.toString().trim();
        }

        return "Không nhận được phản hồi từ AI.";
    }

    private List<Map<String, Object>> buildContentsSingleTurn(String userText) {
        return List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userText))
        ));
    }

    private List<String> parseBullets(String text) {
        if (text == null || text.isBlank()) {
            return List.of("AI không thể tạo bản tóm tắt phù hợp cho nội dung này.");
        }

        List<String> result = new ArrayList<>();
        StringBuilder current = null;

        // 1. Ưu tiên parse bullet nếu có "-"
        for (String line : text.split("\n")) {
            String s = line.trim();

            if (s.startsWith("-")) {
                if (current != null && !current.toString().isBlank()) {
                    result.add(current.toString().trim());
                }
                current = new StringBuilder();
                current.append(s.replaceFirst("^-\\s*", ""));
            } else if (current != null) {
                current.append(" ").append(s);
            }
        }

        if (current != null && !current.toString().isBlank()) {
            result.add(current.toString().trim());
        }

        // 2. Nếu KHÔNG có bullet → tách theo câu
        if (result.isEmpty()) {
            String normalized = text
                    .replace("\n", " ")
                    .replace("“", "\"")
                    .replace("”", "\"")
                    .replace("…", ".");

            String[] sentences = normalized.split("(?<=[.!?]\")\\s+|(?<=[.!?])\\s+");

            for (String s : sentences) {
                String trimmed = s.trim();
                if (trimmed.length() > 40) {
                    result.add(trimmed);
                }
                if (result.size() >= 5) break;
            }
        }

// 3. Nếu vẫn chỉ có 1 câu quá dài → cắt theo độ dài
        if (result.size() == 1 && result.get(0).length() > 200) {
            String longText = result.get(0);
            result.clear();

            int step = longText.length() / 4;
            for (int i = 0; i < 4; i++) {
                int start = i * step;
                int end = (i == 3) ? longText.length() : (i + 1) * step;
                result.add(longText.substring(start, end).trim());
            }
        }

        if (result.isEmpty()) {
            return List.of("AI không thể tạo bản tóm tắt phù hợp cho nội dung này.");
        }

        return result;
    }

    private String htmlToText(String html) {
        // JSoup strip HTML -> text
        return Jsoup.parse(html).text();
    }

    private String shrink(String s, int maxChars) {
        s = s.trim();
        if (s.length() <= maxChars) return s;
        // lấy đầu + cuối để giữ ngữ cảnh
        int head = (int)(maxChars * 0.7);
        int tail = maxChars - head;
        return s.substring(0, head) + "\n...\n" + s.substring(s.length() - tail);
    }

    private String normalizeRole(String role) {
        if (role == null) return "user";
        role = role.trim().toLowerCase(Locale.ROOT);
        return role.equals("model") ? "model" : "user";
    }

    private String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}
