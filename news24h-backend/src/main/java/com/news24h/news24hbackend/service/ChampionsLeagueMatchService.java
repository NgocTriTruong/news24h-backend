package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.ChampionsLeagueMatch;
import com.news24h.news24hbackend.repository.ChampionsLeagueMatchRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChampionsLeagueMatchService {

    private final ChampionsLeagueMatchRepository matchRepository;
    private static final String CURRENT_SEASON = "2025/2026";

    public ChampionsLeagueMatchService(ChampionsLeagueMatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * Crawl lịch thi đấu Cup C1 từ 24h.com.vn
     */
    @Transactional
    public List<ChampionsLeagueMatch> crawlMatches() {
        List<ChampionsLeagueMatch> matches = new ArrayList<>();

        try {
            String url = "https://www.24h.com.vn/bong-da/lich-thi-dau-cup-c1-champions-league-c48a400193.html";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // Thử tìm các link trận đấu
            Elements matchLinks = doc.select("a[href*='/bong-da/']");

            System.out.println("Tìm thấy " + matchLinks.size() + " links bóng đá");

            for (Element link : matchLinks) {
                try {
                    String text = link.text().trim();
                    String href = link.attr("abs:href");

                    // Chỉ xử lý link có đúng pattern trận đấu
                    if (!href.contains("/bong-da/") || text.length() < 10) continue;

                    // Tìm pattern: "Đội A - Đội B" (tìm dấu gạch ngang ở giữa)
                    int dashIndex = text.indexOf(" - ");
                    if (dashIndex > 0 && dashIndex < text.length() - 3) {
                        String homeTeam = text.substring(0, dashIndex).trim();
                        String awayTeam = text.substring(dashIndex + 3).trim();

                        // Nếu awayTeam còn dấu gạch ngang, lấy phần sau cùng
                        int lastDash = awayTeam.lastIndexOf(" - ");
                        if (lastDash > 0) {
                            awayTeam = awayTeam.substring(lastDash + 3).trim();
                        }

                        // Loại bỏ các text không phải tên đội
                        if (homeTeam.matches(".*\\d{4}.*") || awayTeam.matches(".*\\d{4}.*")) continue;
                        if (homeTeam.toLowerCase().contains("trực tiếp") || awayTeam.toLowerCase().contains("trực tiếp")) continue;
                        if (homeTeam.toLowerCase().contains("video") || awayTeam.toLowerCase().contains("video")) continue;
                        if (homeTeam.length() < 3 || awayTeam.length() < 3 || homeTeam.length() > 30 || awayTeam.length() > 30) continue;

                        ChampionsLeagueMatch match = new ChampionsLeagueMatch();
                        match.setHomeTeam(homeTeam);
                        match.setAwayTeam(awayTeam);
                        match.setMatchUrl(href);
                        match.setStatus("scheduled");
                        match.setSeason(CURRENT_SEASON);
                        match.setUpdatedAt(Instant.now());

                        // Tìm thời gian từ parent hoặc sibling elements
                        Element parent = link.parent();
                        if (parent != null) {
                            String parentText = parent.text();
                            // Tìm pattern thời gian: HH:mm DD/MM
                            String timePattern = "\\d{2}:\\d{2}\\s+\\d{1,2}/\\d{1,2}";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(timePattern);
                            java.util.regex.Matcher matcher = pattern.matcher(parentText);
                            if (matcher.find()) {
                                String timeText = matcher.group();
                                match.setMatchTime(timeText);
                                try {
                                    Instant matchDate = parseMatchTime(timeText);
                                    match.setMatchDate(matchDate);
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                        }

                        // Kiểm tra trùng
                        String matchTimeKey = match.getMatchTime() != null ? match.getMatchTime() : "";
                        var existing = matchRepository.findByHomeTeamAndAwayTeamAndMatchTime(
                                homeTeam, awayTeam, matchTimeKey);

                        if (existing.isEmpty()) {
                            matchRepository.save(match);
                            matches.add(match);
                            System.out.println("Đã lưu: " + homeTeam + " vs " + awayTeam);
                        }
                    }

                } catch (Exception e) {
                    // Skip
                }
            }

            System.out.println("Crawl hoàn tất: " + matches.size() + " trận đấu");

        } catch (Exception e) {
            System.out.println("Lỗi crawl lịch thi đấu: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    /**
     * Xóa tất cả và crawl lại từ đầu
     */
    @Transactional
    public List<ChampionsLeagueMatch> refreshMatches() {
        matchRepository.deleteAllBySeason(CURRENT_SEASON);
        System.out.println("Đã xóa tất cả trận đấu cũ");
        return crawlMatches();
    }

    /**
     * Lấy lịch thi đấu
     */
    public List<ChampionsLeagueMatch> getAllMatches() {
        return matchRepository.findBySeasonOrderByMatchDateAsc(CURRENT_SEASON);
    }

    /**
     * Lấy lịch thi đấu sắp tới
     */
    public List<ChampionsLeagueMatch> getUpcomingMatches() {
        return matchRepository.findByMatchDateAfterOrderByMatchDateAsc(Instant.now());
    }

    /**
     * Tự động crawl mỗi 12 giờ
     */
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void autoUpdateMatches() {
        System.out.println("Bắt đầu tự động crawl lịch thi đấu Cup C1...");
        crawlMatches();
    }

    /**
     * Parse thời gian từ text dạng "03:00 20/12"
     */
    private Instant parseMatchTime(String timeText) {
        try {
            // Format: "HH:mm DD/MM"
            String[] parts = timeText.split(" ");
            if (parts.length < 2) return null;

            String time = parts[0]; // "03:00"
            String date = parts[1]; // "20/12"

            String[] dateParts = date.split("/");
            if (dateParts.length < 2) return null;

            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = LocalDateTime.now().getYear(); // Năm hiện tại

            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);
            return dateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

        } catch (Exception e) {
            return null;
        }
    }
}
