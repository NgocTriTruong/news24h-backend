package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.entity.FootballLeague;
import com.news24h.news24hbackend.entity.FootballMatch;
import com.news24h.news24hbackend.entity.FootballStanding;
import com.news24h.news24hbackend.service.FootballDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/football")
@CrossOrigin(origins = "*")
public class FootballController {

    private final FootballDataService footballDataService;

    public FootballController(FootballDataService footballDataService) {
        this.footballDataService = footballDataService;
    }

    // GET /api/football/leagues - Danh sách giải đấu
    @GetMapping("/leagues")
    public ResponseEntity<List<LeagueInfo>> getLeagues() {
        List<LeagueInfo> leagues = Arrays.stream(FootballLeague.values())
                .map(league -> new LeagueInfo(league.getCode(), league.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(leagues);
    }

    // GET /api/football/{leagueCode}/standings - Bảng xếp hạng
    @GetMapping("/{leagueCode}/standings")
    public ResponseEntity<List<FootballStanding>> getStandings(@PathVariable String leagueCode) {
        List<FootballStanding> standings = footballDataService.getStandings(leagueCode);
        return ResponseEntity.ok(standings);
    }

    // POST /api/football/{leagueCode}/standings/crawl - Crawl bảng xếp hạng
    @PostMapping("/{leagueCode}/standings/crawl")
    public ResponseEntity<String> crawlStandings(@PathVariable String leagueCode) {
        FootballLeague league = FootballLeague.fromCode(leagueCode);
        if (league == null) {
            return ResponseEntity.badRequest().body("Giải đấu không tồn tại");
        }

        int count = footballDataService.crawlStandings(league).size();
        return ResponseEntity.ok("Đã crawl bảng xếp hạng " + league.getName() + ": " + count + " đội");
    }

    // GET /api/football/{leagueCode}/matches - Lịch thi đấu
    @GetMapping("/{leagueCode}/matches")
    public ResponseEntity<List<FootballMatch>> getMatches(@PathVariable String leagueCode) {
        List<FootballMatch> matches = footballDataService.getMatches(leagueCode);
        return ResponseEntity.ok(matches);
    }

    // GET /api/football/{leagueCode}/matches/upcoming - Lịch sắp tới
    @GetMapping("/{leagueCode}/matches/upcoming")
    public ResponseEntity<List<FootballMatch>> getUpcomingMatches(@PathVariable String leagueCode) {
        List<FootballMatch> matches = footballDataService.getUpcomingMatches(leagueCode);
        return ResponseEntity.ok(matches);
    }

    // GET /api/football/{leagueCode}/results - Kết quả
    @GetMapping("/{leagueCode}/results")
    public ResponseEntity<List<FootballMatch>> getResults(@PathVariable String leagueCode) {
        List<FootballMatch> results = footballDataService.getResults(leagueCode);
        return ResponseEntity.ok(results);
    }

    // POST /api/football/{leagueCode}/matches/crawl - Crawl lịch thi đấu
    @PostMapping("/{leagueCode}/matches/crawl")
    public ResponseEntity<String> crawlMatches(@PathVariable String leagueCode) {
        FootballLeague league = FootballLeague.fromCode(leagueCode);
        if (league == null) {
            return ResponseEntity.badRequest().body("Giải đấu không tồn tại");
        }

        int count = footballDataService.crawlMatches(league, false).size();
        return ResponseEntity.ok("Đã crawl lịch thi đấu " + league.getName() + ": " + count + " trận");
    }

    // POST /api/football/{leagueCode}/results/crawl - Crawl kết quả
    @PostMapping("/{leagueCode}/results/crawl")
    public ResponseEntity<String> crawlResults(@PathVariable String leagueCode) {
        FootballLeague league = FootballLeague.fromCode(leagueCode);
        if (league == null) {
            return ResponseEntity.badRequest().body("Giải đấu không tồn tại");
        }

        int count = footballDataService.crawlMatches(league, true).size();
        return ResponseEntity.ok("Đã crawl kết quả " + league.getName() + ": " + count + " trận");
    }

    // POST /api/football/crawl-all - Crawl tất cả giải đấu
    @PostMapping("/crawl-all")
    public ResponseEntity<String> crawlAll() {
        StringBuilder result = new StringBuilder();

        for (FootballLeague league : FootballLeague.values()) {
            try {
                int standingCount = footballDataService.crawlStandings(league).size();
                int matchCount = footballDataService.crawlMatches(league, false).size();
                int resultCount = footballDataService.crawlMatches(league, true).size();

                result.append(league.getName())
                        .append(": ")
                        .append(standingCount).append(" đội, ")
                        .append(matchCount).append(" lịch, ")
                        .append(resultCount).append(" kết quả\n");
            } catch (Exception e) {
                result.append(league.getName()).append(": Lỗi - ").append(e.getMessage()).append("\n");
            }
        }

        return ResponseEntity.ok(result.toString());
    }

    // DTO class
    public static class LeagueInfo {
        private String code;
        private String name;

        public LeagueInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}
