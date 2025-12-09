package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.dto.NewsArticleDto;
import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý logic cho News:
 * - Lấy top headlines
 * - Lọc theo category
 * - Search
 * - Xem chi tiết, tăng view
 * - Related news
 * - Tạo/cập nhật bài từ RSS + crawl nội dung
 */
@Service
public class NewsService {

    private final NewsArticleRepository newsRepo;
    private final ArticleCrawlerService crawlerService;

    public NewsService(NewsArticleRepository newsRepo,
                       ArticleCrawlerService crawlerService) {
        this.newsRepo = newsRepo;
        this.crawlerService = crawlerService;
    }

    public List<NewsArticleDto> getTopHeadlines() {
        return newsRepo.findTop10ByOrderByPublishedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<NewsArticleDto> getNewsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepo.findByCategoryOrderByPublishedAtDesc(category, pageable)
                .map(this::toDto);
    }

    public Page<NewsArticleDto> searchNews(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepo.findByTitleContainingIgnoreCase(keyword, pageable)
                .map(this::toDto);
    }

    public NewsArticleDto getById(String id) {
        NewsArticle article = newsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        article.setViewCount(article.getViewCount() + 1);
        newsRepo.save(article);

        return toDto(article);
    }

    public List<NewsArticleDto> getRelated(String articleId) {
        NewsArticle article = newsRepo.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        return newsRepo
                .findTop5ByCategoryAndIdNotOrderByPublishedAtDesc(article.getCategory(), articleId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<NewsArticleDto> getBreakingTicker() {
        // Đơn giản: lấy 5 bài mới nhất
        return newsRepo.findTop10ByOrderByPublishedAtDesc()
                .stream()
                .limit(5)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Tạo hoặc cập nhật bài viết từ dữ liệu RSS:
     * - Dùng sourceUrl để tránh trùng lặp
     * - Crawl nội dung HTML từ bài báo 24h
     */
    public NewsArticleDto createOrUpdateFromRss(String title, String description,
                                                String thumbnail, String category,
                                                String sourceUrl, Instant publishedAt) {

        Optional<NewsArticle> existingOpt = newsRepo.findBySourceUrl(sourceUrl);
        NewsArticle article = existingOpt.orElseGet(NewsArticle::new);

        article.setTitle(title);
        article.setDescription(description);
        article.setThumbnail(thumbnail);
        article.setCategory(category);
        article.setSourceUrl(sourceUrl);
        article.setPublishedAt(publishedAt != null ? publishedAt : Instant.now());

        if (existingOpt.isEmpty() || article.getContent() == null || article.getContent().isBlank()) {
            String contentHtml = crawlerService.crawlHtmlFrom24h(sourceUrl);
            article.setContent(contentHtml);
        }

        if (article.getViewCount() == 0 && existingOpt.isEmpty()) {
            article.setViewCount(0);
        }
        if (existingOpt.isEmpty()) {
            article.setFeatured(false);
        }

        newsRepo.save(article);
        return toDto(article);
    }

    private NewsArticleDto toDto(NewsArticle n) {
        return NewsArticleDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .description(n.getDescription())
                .content(n.getContent())
                .thumbnail(n.getThumbnail())
                .category(n.getCategory())
                .sourceUrl(n.getSourceUrl())
                .publishedAt(n.getPublishedAt())
                .viewCount(n.getViewCount())
                .featured(n.isFeatured())
                .build();
    }
}
