package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.*;
import com.news24h.news24hbackend.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * Tóm tắt bài viết bằng AI.
     * FE có thể gửi articleId hoặc content.
     */
    @PostMapping("/summarize")
    public AiSummaryResponse summarize(@RequestBody AiSummaryRequest request) {
        return aiService.summarize(request);
    }

    /**
     * Chatbot trợ lý ảo.
     * Yêu cầu login (đã cấu hình trong Security).
     */
    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody AiChatRequest request) {
        return aiService.chat(request);
    }
}

