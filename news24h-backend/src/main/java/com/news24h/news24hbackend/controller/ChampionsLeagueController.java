package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.NewsArticleDto;
import com.news24h.news24hbackend.service.ChampionsLeagueNewsService;
import com.news24h.news24hbackend.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/champions-league")
@CrossOrigin(origins = "*")
public class ChampionsLeagueController {

    private final NewsService newsService;
    private final ChampionsLeagueNewsService championsLeagueNewsService;

    public ChampionsLeagueController(NewsService newsService, ChampionsLeagueNewsService championsLeagueNewsService) {
        this.newsService = newsService;
        this.championsLeagueNewsService = championsLeagueNewsService;
    }

    // GET /api/champions-league/news - Lấy tin tức Cup C1
    @GetMapping("/news")
    public ResponseEntity<Page<NewsArticleDto>> getChampionsLeagueNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<NewsArticleDto> news = newsService.getNewsByCategory("cup-c1", page, size);
        return ResponseEntity.ok(news);
    }

    // POST /api/champions-league/news/crawl - Crawl tin Cup C1
    @PostMapping("/news/crawl")
    public ResponseEntity<String> crawlChampionsLeagueNews() {
        int count = championsLeagueNewsService.crawlChampionsLeagueNews().size();
        return ResponseEntity.ok("Đã crawl " + count + " tin tức Cup C1");
    }

    // POST /api/champions-league/news/cleanup - Xóa tin không liên quan
    @PostMapping("/news/cleanup")
    public ResponseEntity<String> cleanupNews() {
        int count = championsLeagueNewsService.cleanupNonChampionsLeagueNews();
        return ResponseEntity.ok("Đã xóa " + count + " tin tức không liên quan đến Cup C1");
    }
}
