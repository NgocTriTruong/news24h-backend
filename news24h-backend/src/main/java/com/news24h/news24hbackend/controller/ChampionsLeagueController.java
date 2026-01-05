package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.NewsArticleDto;
import com.news24h.news24hbackend.entity.ChampionsLeagueMatch;
import com.news24h.news24hbackend.entity.ChampionsLeagueStanding;
import com.news24h.news24hbackend.service.ChampionsLeagueMatchService;
import com.news24h.news24hbackend.service.ChampionsLeagueNewsService;
import com.news24h.news24hbackend.service.ChampionsLeagueStandingService;
import com.news24h.news24hbackend.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/champions-league")
@CrossOrigin(origins = "*")
public class ChampionsLeagueController {

    private final NewsService newsService;
    private final ChampionsLeagueNewsService championsLeagueNewsService;
    private final ChampionsLeagueStandingService standingService;
    private final ChampionsLeagueMatchService matchService;

    public ChampionsLeagueController(NewsService newsService,
                                     ChampionsLeagueNewsService championsLeagueNewsService,
                                     ChampionsLeagueStandingService standingService,
                                     ChampionsLeagueMatchService matchService) {
        this.newsService = newsService;
        this.championsLeagueNewsService = championsLeagueNewsService;
        this.standingService = standingService;
        this.matchService = matchService;
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

    // GET /api/champions-league/standings - Lấy bảng xếp hạng
    @GetMapping("/standings")
    public ResponseEntity<List<ChampionsLeagueStanding>> getStandings() {
        List<ChampionsLeagueStanding> standings = standingService.getCurrentStandings();
        return ResponseEntity.ok(standings);
    }

    // POST /api/champions-league/standings/crawl - Crawl bảng xếp hạng
    @PostMapping("/standings/crawl")
    public ResponseEntity<String> crawlStandings() {
        int count = standingService.crawlStandings().size();
        return ResponseEntity.ok("Đã crawl bảng xếp hạng: " + count + " đội");
    }

    // GET /api/champions-league/matches - Lấy lịch thi đấu
    @GetMapping("/matches")
    public ResponseEntity<List<ChampionsLeagueMatch>> getMatches() {
        List<ChampionsLeagueMatch> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }

    // GET /api/champions-league/matches/upcoming - Lấy lịch sắp tới
    @GetMapping("/matches/upcoming")
    public ResponseEntity<List<ChampionsLeagueMatch>> getUpcomingMatches() {
        List<ChampionsLeagueMatch> matches = matchService.getUpcomingMatches();
        return ResponseEntity.ok(matches);
    }

    // POST /api/champions-league/matches/crawl - Crawl lịch thi đấu
    @PostMapping("/matches/crawl")
    public ResponseEntity<String> crawlMatches() {
        int count = matchService.crawlMatches().size();
        return ResponseEntity.ok("Đã crawl lịch thi đấu: " + count + " trận");
    }

    // POST /api/champions-league/matches/refresh - Xóa và crawl lại
    @PostMapping("/matches/refresh")
    public ResponseEntity<String> refreshMatches() {
        int count = matchService.refreshMatches().size();
        return ResponseEntity.ok("Đã làm mới lịch thi đấu: " + count + " trận");
    }
}
