package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.service.PoliticsSocietyNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politics-society")
@CrossOrigin(origins = "*")
public class PoliticsSocietyNewsController {

    private final PoliticsSocietyNewsService politicsSocietyNewsService;

    public PoliticsSocietyNewsController(PoliticsSocietyNewsService politicsSocietyNewsService) {
        this.politicsSocietyNewsService = politicsSocietyNewsService;
    }

    /**
     * Crawl tin tức Chính trị - Xã hội
     */
    @PostMapping("/news/crawl")
    public ResponseEntity<String> crawlPoliticsSocietyNews() {
        List<NewsArticle> articles = politicsSocietyNewsService.crawlPoliticsSocietyNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin tức Chính trị - Xã hội");
    }
}
