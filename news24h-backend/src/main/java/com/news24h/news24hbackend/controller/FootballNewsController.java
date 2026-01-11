package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/football")
@CrossOrigin(origins = "*")
public class FootballNewsController {

    private final PremierLeagueNewsService premierLeagueNewsService;
    private final BundesligaNewsService bundesligaNewsService;
    private final SerieANewsService serieANewsService;
    private final LaLigaNewsService laLigaNewsService;
    private final VLeagueNewsService vLeagueNewsService;

    public FootballNewsController(PremierLeagueNewsService premierLeagueNewsService,
                                  BundesligaNewsService bundesligaNewsService,
                                  SerieANewsService serieANewsService,
                                  LaLigaNewsService laLigaNewsService,
                                  VLeagueNewsService vLeagueNewsService) {
        this.premierLeagueNewsService = premierLeagueNewsService;
        this.bundesligaNewsService = bundesligaNewsService;
        this.serieANewsService = serieANewsService;
        this.laLigaNewsService = laLigaNewsService;
        this.vLeagueNewsService = vLeagueNewsService;
    }

    @PostMapping("/ngoai-hang-anh/crawl")
    public ResponseEntity<String> crawlPremierLeague() {
        List<NewsArticle> articles = premierLeagueNewsService.crawlPremierLeagueNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Ngoại hạng Anh");
    }

    @PostMapping("/bundesliga/crawl")
    public ResponseEntity<String> crawlBundesliga() {
        List<NewsArticle> articles = bundesligaNewsService.crawlBundesligaNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bundesliga");
    }

    @PostMapping("/serie-a/crawl")
    public ResponseEntity<String> crawlSerieA() {
        List<NewsArticle> articles = serieANewsService.crawlSerieANews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Serie A");
    }

    @PostMapping("/la-liga/crawl")
    public ResponseEntity<String> crawlLaLiga() {
        List<NewsArticle> articles = laLigaNewsService.crawlLaLigaNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin La Liga");
    }

    @PostMapping("/v-league/crawl")
    public ResponseEntity<String> crawlVLeague() {
        List<NewsArticle> articles = vLeagueNewsService.crawlVLeagueNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin V-League");
    }

    @PostMapping("/news/crawl-all")
    public ResponseEntity<String> crawlAllFootballNews() {
        int total = 0;
        StringBuilder result = new StringBuilder();

        List<NewsArticle> premierLeague = premierLeagueNewsService.crawlPremierLeagueNews();
        total += premierLeague.size();
        result.append("Ngoại hạng Anh: ").append(premierLeague.size()).append(" tin\n");

        List<NewsArticle> bundesliga = bundesligaNewsService.crawlBundesligaNews();
        total += bundesliga.size();
        result.append("Bundesliga: ").append(bundesliga.size()).append(" tin\n");

        List<NewsArticle> serieA = serieANewsService.crawlSerieANews();
        total += serieA.size();
        result.append("Serie A: ").append(serieA.size()).append(" tin\n");

        List<NewsArticle> laLiga = laLigaNewsService.crawlLaLigaNews();
        total += laLiga.size();
        result.append("La Liga: ").append(laLiga.size()).append(" tin\n");

        List<NewsArticle> vLeague = vLeagueNewsService.crawlVLeagueNews();
        total += vLeague.size();
        result.append("V-League: ").append(vLeague.size()).append(" tin\n");

        result.append("\nTổng: ").append(total).append(" tin");
        return ResponseEntity.ok(result.toString());
    }
}
