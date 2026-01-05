package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.ChampionsLeagueStanding;
import com.news24h.news24hbackend.repository.ChampionsLeagueStandingRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChampionsLeagueStandingService {

    private final ChampionsLeagueStandingRepository standingRepository;
    private static final String CURRENT_SEASON = "2025/2026";

    public ChampionsLeagueStandingService(ChampionsLeagueStandingRepository standingRepository) {
        this.standingRepository = standingRepository;
    }

    /**
     * Crawl bảng xếp hạng Cup C1 từ 24h.com.vn
     */
    @Transactional
    public List<ChampionsLeagueStanding> crawlStandings() {
        List<ChampionsLeagueStanding> standings = new ArrayList<>();

        try {
            String url = "https://www.24h.com.vn/bong-da/bang-xep-hang-cup-c1-champions-league-c48a400193.html";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // Tìm bảng xếp hạng
            Elements rows = doc.select("table tbody tr");

            System.out.println("Tìm thấy " + rows.size() + " đội trong bảng xếp hạng");

            // Xóa dữ liệu cũ của mùa giải hiện tại
            standingRepository.deleteAllBySeason(CURRENT_SEASON);

            for (Element row : rows) {
                try {
                    Elements cells = row.select("td");
                    if (cells.size() < 10) continue;

                    ChampionsLeagueStanding standing = new ChampionsLeagueStanding();

                    // Thứ tự
                    String positionText = cells.get(0).text().trim();
                    standing.setPosition(parseInteger(positionText));

                    // Tên đội
                    Element teamCell = cells.get(1);
                    String teamName = teamCell.text().trim();
                    standing.setTeamName(teamName);

                    // Logo đội (nếu có)
                    Element teamLogo = teamCell.selectFirst("img");
                    if (teamLogo != null) {
                        standing.setTeamLogo(teamLogo.attr("abs:src"));
                    }

                    // Số trận
                    standing.setMatchesPlayed(parseInteger(cells.get(3).text()));

                    // Thắng
                    standing.setWins(parseInteger(cells.get(4).text()));

                    // Hòa
                    standing.setDraws(parseInteger(cells.get(5).text()));

                    // Thua
                    standing.setLosses(parseInteger(cells.get(6).text()));

                    // Bàn thắng
                    standing.setGoalsFor(parseInteger(cells.get(7).text()));

                    // Bàn thua
                    standing.setGoalsAgainst(parseInteger(cells.get(8).text()));

                    // Hiệu số
                    standing.setGoalDifference(parseInteger(cells.get(9).text()));

                    // Điểm
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
                            form.setLength(form.length() - 1); // Bỏ dấu - cuối
                        }
                        standing.setRecentForm(form.toString());
                    }

                    standing.setSeason(CURRENT_SEASON);
                    standing.setUpdatedAt(Instant.now());

                    standingRepository.save(standing);
                    standings.add(standing);

                    System.out.println("Đã lưu: " + teamName + " - " + standing.getPoints() + " điểm");

                } catch (Exception e) {
                    System.out.println("Lỗi parse dòng: " + e.getMessage());
                }
            }

            System.out.println("Crawl hoàn tất: " + standings.size() + " đội");

        } catch (Exception e) {
            System.out.println("Lỗi crawl bảng xếp hạng: " + e.getMessage());
            e.printStackTrace();
        }

        return standings;
    }

    /**
     * Lấy bảng xếp hạng hiện tại
     */
    public List<ChampionsLeagueStanding> getCurrentStandings() {
        return standingRepository.findBySeasonOrderByPositionAsc(CURRENT_SEASON);
    }

    /**
     * Tự động crawl mỗi 6 giờ
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 giờ
    public void autoUpdateStandings() {
        System.out.println("Bắt đầu tự động crawl bảng xếp hạng Cup C1...");
        crawlStandings();
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
}
