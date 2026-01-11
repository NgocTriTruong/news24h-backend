package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.service.BusinessNewsService;
import com.news24h.news24hbackend.service.StockMarketNewsService;
import com.news24h.news24hbackend.service.StartupNewsService;
import com.news24h.news24hbackend.service.RealEstateNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@CrossOrigin(origins = "*")
public class BusinessNewsController {

    private final BusinessNewsService businessNewsService;
    private final StockMarketNewsService stockMarketNewsService;
    private final StartupNewsService startupNewsService;
    private final RealEstateNewsService realEstateNewsService;

    public BusinessNewsController(BusinessNewsService businessNewsService,
                                  StockMarketNewsService stockMarketNewsService,
                                  StartupNewsService startupNewsService,
                                  RealEstateNewsService realEstateNewsService) {
        this.businessNewsService = businessNewsService;
        this.stockMarketNewsService = stockMarketNewsService;
        this.startupNewsService = startupNewsService;
        this.realEstateNewsService = realEstateNewsService;
    }

    @PostMapping("/doanh-nghiep/crawl")
    public ResponseEntity<String> crawlBusinessNews() {
        List<NewsArticle> articles = businessNewsService.crawlBusinessNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Doanh nghiệp");
    }

    @PostMapping("/chung-khoan/crawl")
    public ResponseEntity<String> crawlStockMarketNews() {
        List<NewsArticle> articles = stockMarketNewsService.crawlStockMarketNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Chứng khoán");
    }

    @PostMapping("/khoi-nghiep/crawl")
    public ResponseEntity<String> crawlStartupNews() {
        List<NewsArticle> articles = startupNewsService.crawlStartupNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Khởi nghiệp");
    }

    @PostMapping("/bat-dong-san/crawl")
    public ResponseEntity<String> crawlRealEstateNews() {
        List<NewsArticle> articles = realEstateNewsService.crawlRealEstateNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bất động sản");
    }

    @PostMapping("/crawl-all")
    public ResponseEntity<String> crawlAllBusinessNews() {
        int total = 0;
        StringBuilder result = new StringBuilder();

        List<NewsArticle> business = businessNewsService.crawlBusinessNews();
        total += business.size();
        result.append("Doanh nghiệp: ").append(business.size()).append(" tin\n");

        List<NewsArticle> stockMarket = stockMarketNewsService.crawlStockMarketNews();
        total += stockMarket.size();
        result.append("Chứng khoán: ").append(stockMarket.size()).append(" tin\n");

        List<NewsArticle> startup = startupNewsService.crawlStartupNews();
        total += startup.size();
        result.append("Khởi nghiệp: ").append(startup.size()).append(" tin\n");

        List<NewsArticle> realEstate = realEstateNewsService.crawlRealEstateNews();
        total += realEstate.size();
        result.append("Bất động sản: ").append(realEstate.size()).append(" tin\n");

        result.append("\nTổng: ").append(total).append(" tin");
        return ResponseEntity.ok(result.toString());
    }
}
