package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.NewsArticleDto;
import com.news24h.news24hbackend.service.ArticleCrawlerService;
import com.news24h.news24hbackend.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API chính cho phần tin tức:
 * - /api/news/top-headlines
 * - /api/news/category/{slug}
 * - /api/news/{id}
 * - /api/news/search
 * - /api/news/breaking-ticker
 * - /api/news/{id}/crawl (dev)
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;
    private final ArticleCrawlerService crawlerService;

    public NewsController(NewsService newsService, ArticleCrawlerService crawlerService) {
        this.newsService = newsService;
        this.crawlerService = crawlerService;
    }

    @GetMapping("/top-headlines")
    public List<NewsArticleDto> getTopHeadlines() {
        return newsService.getTopHeadlines();
    }

    @GetMapping("/category/{slug}")
    public Page<NewsArticleDto> getByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return newsService.getNewsByCategory(slug, page, size);
    }

    @GetMapping("/{id}")
    public NewsArticleDto getById(@PathVariable String id) {
        return newsService.getById(id);
    }

    @GetMapping("/search")
    public Page<NewsArticleDto> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return newsService.searchNews(query, page, size);
    }

    @GetMapping("/{id}/related")
    public List<NewsArticleDto> related(@PathVariable String id) {
        return newsService.getRelated(id);
    }

    @GetMapping("/breaking-ticker")
    public List<NewsArticleDto> breakingTicker() {
        return newsService.getBreakingTicker();
    }

    // Dev endpoint: crawl HTML nội dung từ sourceUrl rồi lưu vào DB
    @PostMapping("/{id}/crawl")
    public void crawlContent(@PathVariable String id) {
        crawlerService.crawlContentForArticle(id);
    }
}

