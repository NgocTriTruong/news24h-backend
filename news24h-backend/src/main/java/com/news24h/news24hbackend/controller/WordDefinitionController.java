package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.WordDefinitionRequest;
import com.news24h.news24hbackend.dto.WordDefinitionResponse;
import com.news24h.news24hbackend.service.WordDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definitions")
@RequiredArgsConstructor
public class WordDefinitionController {

    private final WordDefinitionService wordDefinitionService;

    /**
     * Lấy định nghĩa của một từ
     * POST /api/definitions/lookup
     * Body: { "word": "từ cần giải thích", "language": "vi" }
     */
    @PostMapping("/lookup")
    public ResponseEntity<WordDefinitionResponse> lookup(@RequestBody WordDefinitionRequest request) {
        WordDefinitionResponse response = wordDefinitionService.getDefinition(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy định nghĩa (method GET - tùy chọn)
     * GET /api/definitions/lookup?word=từ&language=vi
     */
    @GetMapping("/lookup")
    public ResponseEntity<WordDefinitionResponse> lookupGet(
            @RequestParam String word,
            @RequestParam(defaultValue = "vi") String language) {
        WordDefinitionRequest request = new WordDefinitionRequest(word, language);
        WordDefinitionResponse response = wordDefinitionService.getDefinition(request);
        return ResponseEntity.ok(response);
    }
}
