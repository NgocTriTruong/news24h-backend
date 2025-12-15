package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.GoldPriceDto;
import com.news24h.news24hbackend.dto.NewsArticleDto;
import com.news24h.news24hbackend.entity.GoldPrice;
import com.news24h.news24hbackend.service.GoldNewsService;
import com.news24h.news24hbackend.service.GoldPriceService;
import com.news24h.news24hbackend.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gold-prices")
@CrossOrigin(origins = "*")
public class GoldPriceController {

    private final GoldPriceService goldPriceService;
    private final NewsService newsService;
    private final GoldNewsService goldNewsService;

    public GoldPriceController(GoldPriceService goldPriceService, NewsService newsService, GoldNewsService goldNewsService) {
        this.goldPriceService = goldPriceService;
        this.newsService = newsService;
        this.goldNewsService = goldNewsService;
    }

    // GET /api/gold-prices - Lấy tất cả giá vàng
    @GetMapping
    public ResponseEntity<List<GoldPriceDto>> getAllGoldPrices() {
        List<GoldPrice> goldPrices = goldPriceService.getAllGoldPrices();
        List<GoldPriceDto> dtos = goldPrices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // GET /api/gold-prices/company/{company} - Lấy giá vàng theo công ty
    @GetMapping("/company/{company}")
    public ResponseEntity<List<GoldPriceDto>> getGoldPricesByCompany(@PathVariable String company) {
        List<GoldPrice> goldPrices = goldPriceService.getGoldPricesByCompany(company);
        List<GoldPriceDto> dtos = goldPrices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST /api/gold-prices/crawl - Crawl giá vàng thủ công
    @PostMapping("/crawl")
    public ResponseEntity<List<GoldPriceDto>> crawlGoldPrices() {
        List<GoldPrice> goldPrices = goldPriceService.crawlGoldPrices();
        List<GoldPriceDto> dtos = goldPrices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // GET /api/gold-prices/news - Lấy tin tức về giá vàng
    @GetMapping("/news")
    public ResponseEntity<Page<NewsArticleDto>> getGoldNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<NewsArticleDto> goldNews = newsService.getNewsByCategory("gia-vang", page, size);
        return ResponseEntity.ok(goldNews);
    }

    // POST /api/gold-prices/news/crawl - Crawl tin tức giá vàng
    @PostMapping("/news/crawl")
    public ResponseEntity<String> crawlGoldNews() {
        int count = goldNewsService.crawlGoldNews().size();
        return ResponseEntity.ok("Đã crawl " + count + " tin tức giá vàng từ giavang.net");
    }

    // POST /api/gold-prices/news/refresh-content - Crawl lại content cho tin đã có
    @PostMapping("/news/refresh-content")
    public ResponseEntity<String> refreshGoldNewsContent() {
        int count = goldNewsService.refreshContentForExistingNews();
        return ResponseEntity.ok("Đã refresh content cho " + count + " tin tức");
    }

    // POST /api/gold-prices/news/cleanup - Xóa tin không phải từ giavang.net
    @PostMapping("/news/cleanup")
    public ResponseEntity<String> cleanupNonGoldNews() {
        int count = goldNewsService.cleanupNonGoldNews();
        return ResponseEntity.ok("Đã xóa " + count + " tin tức không liên quan đến giá vàng");
    }

    // Helper method để convert entity sang DTO
    private GoldPriceDto convertToDto(GoldPrice goldPrice) {
        GoldPriceDto dto = new GoldPriceDto();
        dto.setId(goldPrice.getId());
        dto.setGoldType(goldPrice.getGoldType());
        dto.setBuyPrice(goldPrice.getBuyPrice());
        dto.setSellPrice(goldPrice.getSellPrice());
        dto.setCompany(goldPrice.getCompany());
        dto.setCrawledAt(goldPrice.getCrawledAt());
        dto.setUpdatedAt(goldPrice.getUpdatedAt());
        return dto;
    }
}
