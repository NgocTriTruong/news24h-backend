package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.FootballLeague;
import com.news24h.news24hbackend.entity.FootballMatch;
import com.news24h.news24hbackend.entity.FootballStanding;
import com.news24h.news24hbackend.repository.FootballMatchRepository;
import com.news24h.news24hbackend.repository.FootballStandingRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class FootballDataService {

    private final FootballStandingRepository standingRepository;
    private final FootballMatchRepository matchRepository;
    private static final String CURRENT_SEASON = "2025/2026";

    public FootballDataService(FootballStandingRepository standingRepository,
                               FootballMatchRepository matchRepository) {
        this.standingRepository = standingRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Crawl bảng xếp hạng của một giải đấu
     */
    @Transactional
    public List<FootballStanding> crawlStandings(FootballLeague league) {
        List<FootballStanding> standings = new ArrayList<>();

        try {
            String url = league.getStandingsUrl();

            System.out.println("[" + league.getName() + "] Đang crawl từ: " + url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // Chỉ dùng selector đơn giản
            Elements rows = doc.select("table tbody tr");
            System.out.println("[" + league.getName() + "] Tìm thấy " + rows.size() + " dòng");

            // Xóa dữ liệu cũ
            standingRepository.deleteAllByLeagueCodeAndSeason(league.getCode(), CURRENT_SEASON);

            for (Element row : rows) {
                try {
                    Elements cells = row.select("td");
                    if (cells.size() < 10) continue;

                    FootballStanding standing = new FootballStanding();
                    standing.setLeagueCode(league.getCode());
                    standing.setLeagueName(league.getName());

                    standing.setPosition(parseInteger(cells.get(0).text()));

                    Element teamCell = cells.get(1);
                    standing.setTeamName(teamCell.text().trim());

                    Element teamLogo = teamCell.selectFirst("img");
                    if (teamLogo != null) {
                        standing.setTeamLogo(teamLogo.attr("abs:src"));
                    }

                    standing.setMatchesPlayed(parseInteger(cells.get(3).text()));
                    standing.setWins(parseInteger(cells.get(4).text()));
                    standing.setDraws(parseInteger(cells.get(5).text()));
                    standing.setLosses(parseInteger(cells.get(6).text()));
                    standing.setGoalsFor(parseInteger(cells.get(7).text()));
                    standing.setGoalsAgainst(parseInteger(cells.get(8).text()));
                    standing.setGoalDifference(parseInteger(cells.get(9).text()));
                    standing.setPoints(parseInteger(cells.get(10).text()));

                    // Kết quả 5 trận gần nhất
                    if (cells.size() > 11) {
                        Element formCell = cells.get(11);
                        Elements formIcons = formCell.select("span");
                        StringBuilder form = new StringBuilder();
                        for (Element icon : formIcons) {
                            String className = icon.className();
                            if (className.contains("green") || className.contains("win")) {
                                form.append("W-");
                            } else if (className.contains("red") || className.contains("lose")) {
                                form.append("L-");
                            } else if (className.contains("gray") || className.contains("draw")) {
                                form.append("D-");
                            }
                        }
                        if (form.length() > 0) {
                            form.setLength(form.length() - 1);
                        }
                        standing.setRecentForm(form.toString());
                    }

                    standing.setSeason(CURRENT_SEASON);
                    standing.setUpdatedAt(Instant.now());

                    standingRepository.save(standing);
                    standings.add(standing);

                } catch (Exception e) {
                    // Skip
                }
            }

            System.out.println("[" + league.getName() + "] Crawl hoàn tất: " + standings.size() + " đội");

        } catch (Exception e) {
            System.out.println("[" + league.getName() + "] Lỗi crawl bảng xếp hạng: " + e.getMessage());
        }

        return standings;
    }

    /**
     * Crawl lịch thi đấu/kết quả của một giải
     */
    @Transactional
    public List<FootballMatch> crawlMatches(FootballLeague league, boolean isResult) {
        List<FootballMatch> matches = new ArrayList<>();

        try {
            String url = isResult ? league.getResultsUrl() : league.getScheduleUrl();

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // Selector mới: tim cac div chua tran dau
            Elements matchDivs = doc.select("div.cate-24h-foot-home-sche-content__match");
            System.out.println("[" + league.getName() + "] Tim thay " + matchDivs.size() + " tran dau");

            for (Element matchDiv : matchDivs) {
                try {
                    FootballMatch match = new FootballMatch();
                    match.setLeagueCode(league.getCode());
                    match.setLeagueName(league.getName());
                    match.setSeason(CURRENT_SEASON);

                    // Doi nha (ben trai)
                    Element leftDiv = matchDiv.selectFirst("div.cate-24h-foot-home-sche-content__match--left");
                    if (leftDiv != null) {
                        Element homeTeamName = leftDiv.selectFirst("figcaption span");
                        Element homeTeamLogo = leftDiv.selectFirst("figure img");
                        if (homeTeamName != null) {
                            match.setHomeTeam(homeTeamName.text().trim());
                        }
                        if (homeTeamLogo != null) {
                            match.setHomeLogo(homeTeamLogo.attr("abs:src"));
                        }
                    }

                    // Doi khach (ben phai)
                    Element rightDiv = matchDiv.selectFirst("div.cate-24h-foot-home-sche-content__match--right");
                    if (rightDiv != null) {
                        Element awayTeamName = rightDiv.selectFirst("figcaption span");
                        Element awayTeamLogo = rightDiv.selectFirst("figure img");
                        if (awayTeamName != null) {
                            match.setAwayTeam(awayTeamName.text().trim());
                        }
                        if (awayTeamLogo != null) {
                            match.setAwayLogo(awayTeamLogo.attr("abs:src"));
                        }
                    }

                    // Trung tam: thoi gian, ty so, link
                    Element centerDiv = matchDiv.selectFirst("div.cate-24h-foot-home-sche-content__match--center");
                    if (centerDiv != null) {
                        // Link tran dau
                        Element link = centerDiv.selectFirst("a.link-ls-table");
                        if (link != null) {
                            match.setMatchUrl(link.attr("abs:href"));
                        }

                        // Lay text full de parse
                        String fullText = centerDiv.text();

                        // Tim ty so (dang "2 - 1" hoac "2-1")
                        if (fullText.matches(".*\\d+\\s*-\\s*\\d+.*")) {
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");
                            java.util.regex.Matcher matcher = pattern.matcher(fullText);
                            if (matcher.find()) {
                                match.setHomeScore(Integer.parseInt(matcher.group(1)));
                                match.setAwayScore(Integer.parseInt(matcher.group(2)));
                                match.setStatus("finished");
                            }
                        } else {
                            match.setStatus("scheduled");
                        }
                    }

                    // Tim thoi gian (trong grandparent)
                    Element parent = matchDiv.parent();
                    if (parent != null) {
                        Element grandParent = parent.parent();
                        if (grandParent != null) {
                            Element timeElement = grandParent.selectFirst("time.cate-24h-foot-home-sche-content__time");
                            if (timeElement != null) {
                                String timeText = timeElement.text().trim();
                                match.setMatchTime(timeText);

                                // Parse thoi gian (format: "19:30 20/12" hoac "20/12")
                                try {
                                    if (timeText.matches("\\d{1,2}:\\d{2}\\s+\\d{1,2}/\\d{1,2}")) {
                                        // Co gio va ngay: "19:30 20/12"
                                        String[] parts = timeText.split("\\s+");
                                        if (parts.length == 2) {
                                            String[] dateParts = parts[1].split("/");
                                            if (dateParts.length == 2) {
                                                int day = Integer.parseInt(dateParts[0]);
                                                int month = Integer.parseInt(dateParts[1]);
                                                int year = java.time.LocalDate.now().getYear();

                                                String[] timeParts = parts[0].split(":");
                                                int hour = Integer.parseInt(timeParts[0]);
                                                int minute = Integer.parseInt(timeParts[1]);

                                                java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(year, month, day, hour, minute);
                                                match.setMatchDate(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                                            }
                                        }
                                    } else if (timeText.matches("\\d{1,2}/\\d{1,2}")) {
                                        // Chi co ngay: "20/12"
                                        String[] dateParts = timeText.split("/");
                                        if (dateParts.length == 2) {
                                            int day = Integer.parseInt(dateParts[0]);
                                            int month = Integer.parseInt(dateParts[1]);
                                            int year = java.time.LocalDate.now().getYear();

                                            java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(year, month, day, 0, 0);
                                            match.setMatchDate(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                                        }
                                    }
                                } catch (Exception e) {
                                    // Ignore parse errors
                                }
                            }
                        }
                    }

                    match.setUpdatedAt(Instant.now());

                    // Kiem tra du lieu hop le va khong trung
                    if (match.getHomeTeam() != null && match.getAwayTeam() != null &&
                            match.getHomeTeam().length() > 2 && match.getAwayTeam().length() > 2) {

                        String matchTimeKey = match.getMatchTime() != null ? match.getMatchTime() : "";
                        var existing = matchRepository.findByLeagueCodeAndHomeTeamAndAwayTeamAndMatchTime(
                                league.getCode(), match.getHomeTeam(), match.getAwayTeam(), matchTimeKey);

                        if (existing.isEmpty()) {
                            matchRepository.save(match);
                            matches.add(match);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("  Skip tran: " + e.getMessage());
                }
            }

            System.out.println("[" + league.getName() + "] Crawl hoan tat: " + matches.size() + " tran");

        } catch (Exception e) {
            System.out.println("[" + league.getName() + "] Loi crawl lich thi dau: " + e.getMessage());
        }

        return matches;
    }

    /**
     * Lấy bảng xếp hạng
     */
    public List<FootballStanding> getStandings(String leagueCode) {
        return standingRepository.findByLeagueCodeAndSeasonOrderByPositionAsc(leagueCode, CURRENT_SEASON);
    }

    /**
     * Lấy lịch thi đấu
     */
    public List<FootballMatch> getMatches(String leagueCode) {
        return matchRepository.findByLeagueCodeAndSeasonOrderByMatchDateDesc(leagueCode, CURRENT_SEASON);
    }

    /**
     * Lấy kết quả
     */
    public List<FootballMatch> getResults(String leagueCode) {
        return matchRepository.findByLeagueCodeAndStatusOrderByMatchDateAsc(leagueCode, "finished");
    }

    /**
     * Lấy lịch sắp tới
     */
    public List<FootballMatch> getUpcomingMatches(String leagueCode) {
        return matchRepository.findByLeagueCodeAndMatchDateAfterOrderByMatchDateAsc(leagueCode, Instant.now());
    }

    /**
     * Tự động crawl tất cả giải đấu khi khởi động và mỗi 12h
     */
    @Scheduled(fixedRate = 43200000, initialDelay = 20000)
    public void scheduledCrawlAllLeagues() {
        System.out.println("[FootballDataService] Tự động crawl tất cả giải đấu...");
        for (FootballLeague league : FootballLeague.values()) {
            try {
                crawlStandings(league);
                crawlMatches(league, false); // Lịch thi đấu
                crawlMatches(league, true);  // Kết quả
            } catch (Exception e) {
                System.out.println("[FootballDataService] Lỗi crawl cho " + league.getName() + ": " + e.getMessage());
            }
        }
    }

    private Integer parseInteger(String text) {
        try {
            text = text.replaceAll("[^0-9-]", "").trim();
            if (text.isEmpty()) return 0;
            return Integer.parseInt(text);
        } catch (Exception e) {
            return 0;
        }
    }

    private Instant parseMatchTime(String timeText) {
        try {
            String[] parts = timeText.split(" ");
            if (parts.length < 2) return null;

            String time = parts[0];
            String date = parts[1];

            String[] dateParts = date.split("/");
            if (dateParts.length < 2) return null;

            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = LocalDateTime.now().getYear();

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
