package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/football-news")
@CrossOrigin(origins = "*")
public class FootballNewsExtraController {

    private final TransferNewsService transferNewsService;
    private final FootballStarNewsService footballStarNewsService;
    private final FootballBriefNewsService footballBriefNewsService;

    public FootballNewsExtraController(
            TransferNewsService transferNewsService,
            FootballStarNewsService footballStarNewsService,
            FootballBriefNewsService footballBriefNewsService) {
        this.transferNewsService = transferNewsService;
        this.footballStarNewsService = footballStarNewsService;
        this.footballBriefNewsService = footballBriefNewsService;

    }



    @PostMapping("/chuyen-nhuong/crawl")
    public ResponseEntity<String> crawlTransfer() {
        List<NewsArticle> articles = transferNewsService.crawlTransferNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Chuyển nhượng");
    }

    @PostMapping("/ngoi-sao/crawl")
    public ResponseEntity<String> crawlFootballStar() {
        List<NewsArticle> articles = footballStarNewsService.crawlFootballStarNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Ngôi sao bóng đá");
    }

    @PostMapping("/diem-tin/crawl")
    public ResponseEntity<String> crawlFootballBrief() {
        List<NewsArticle> articles = footballBriefNewsService.crawlFootballBriefNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Điểm tin bóng đá");
    }



    @PostMapping("/crawl-all")
    public ResponseEntity<String> crawlAll() {
        int total = 0;
        StringBuilder result = new StringBuilder();


        List<NewsArticle> transfer = transferNewsService.crawlTransferNews();
        total += transfer.size();
        result.append("Chuyển nhượng: ").append(transfer.size()).append(" tin\n");

        List<NewsArticle> star = footballStarNewsService.crawlFootballStarNews();
        total += star.size();
        result.append("Ngôi sao bóng đá: ").append(star.size()).append(" tin\n");

        List<NewsArticle> brief = footballBriefNewsService.crawlFootballBriefNews();
        total += brief.size();
        result.append("Điểm tin bóng đá: ").append(brief.size()).append(" tin\n");


        result.append("\nTổng: ").append(total).append(" tin");
        return ResponseEntity.ok(result.toString());
    }
}
