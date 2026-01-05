package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.NewsArticleDto;
import com.news24h.news24hbackend.service.ArticleCrawlerService;
import com.news24h.news24hbackend.service.NewsService;
import com.news24h.news24hbackend.service.ThumbnailUpdateService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ThumbnailUpdateService thumbnailUpdateService;

    public NewsController(NewsService newsService, ArticleCrawlerService crawlerService, ThumbnailUpdateService thumbnailUpdateService) {
        this.newsService = newsService;
        this.crawlerService = crawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;
    }

    // Lấy danh sách tiêu đề tin tức nổi bật
    @GetMapping("/top-headlines")
    public List<NewsArticleDto> getTopHeadlines() {
        return newsService.getTopHeadlines();
    }

    // Lấy danh sách tin tức theo category với phân trang
    @GetMapping("/category/{slug}")
    public Page<NewsArticleDto> getByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return newsService.getNewsByCategory(slug, page, size);
    }

    // Lấy chi tiết bài viết theo ID
    @GetMapping("/{id}")
    public NewsArticleDto getById(@PathVariable String id) {
        return newsService.getById(id);
    }

    // Tìm kiếm bài viết theo từ khóa với phân trang
    @GetMapping("/search")
    public Page<NewsArticleDto> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return newsService.searchNews(query, page, size);
    }

    // Lấy danh sách bài viết liên quan
    @GetMapping("/{id}/related")
    public List<NewsArticleDto> related(@PathVariable String id) {
        return newsService.getRelated(id);
    }

    // Lấy danh sách tin tức dạng ticker (dành cho thanh tin tức chạy ngang)
    @GetMapping("/breaking-ticker")
    public List<NewsArticleDto> breakingTicker() {
        return newsService.getBreakingTicker();
    }

    // Dev endpoint: crawl HTML nội dung từ sourceUrl rồi lưu vào DB
    @PostMapping("/{id}/crawl")
    public void crawlContent(@PathVariable String id) {
        crawlerService.crawlContentForArticle(id);
    }

    // Update thumbnails cho các bài viết đang dùng placeholder
    @PostMapping("/update-thumbnails")
    public Map<String, Object> updateThumbnails() {
        int updated = thumbnailUpdateService.updatePlaceholderThumbnails();
        Map<String, Object> result = new HashMap<>();
        result.put("updated", updated);
        result.put("message", "Đã update " + updated + " thumbnails");
        return result;
    }

    // Update thumbnail cho một bài viết cụ thể
    @PostMapping("/{id}/update-thumbnail")
    public Map<String, Object> updateThumbnail(@PathVariable String id) {
        boolean success = thumbnailUpdateService.updateThumbnailForArticle(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Thumbnail updated" : "Failed to update thumbnail");
        return result;
    }
}

