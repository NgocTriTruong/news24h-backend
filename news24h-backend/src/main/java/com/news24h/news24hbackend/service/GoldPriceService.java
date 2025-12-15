package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.GoldPrice;
import com.news24h.news24hbackend.repository.GoldPriceRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoldPriceService {

    private final GoldPriceRepository goldPriceRepository;

    public GoldPriceService(GoldPriceRepository goldPriceRepository) {
        this.goldPriceRepository = goldPriceRepository;
    }

    // Crawl giá vàng từ doji.vn
    public List<GoldPrice> crawlGoldPrices() {
        List<GoldPrice> goldPrices = new ArrayList<>();

        try {
            // Thử crawl từ Doji
            String url = "https://www.doji.vn/bang-gia-vang.html";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            LocalDateTime now = LocalDateTime.now();

            // Tìm bảng giá
            Elements rows = doc.select("table tr");

            System.out.println("Tìm thấy " + rows.size() + " rows");

            for (Element row : rows) {
                Elements cols = row.select("td");

                if (cols.size() >= 3) {
                    try {
                        String goldType = cols.get(0).text().trim();

                        // Bỏ qua header
                        if (goldType.isEmpty() || goldType.toLowerCase().contains("loại") ||
                                goldType.toLowerCase().contains("sản phẩm")) {
                            continue;
                        }

                        String buyPriceStr = cols.get(1).text().replaceAll("[^0-9.]", "");
                        String sellPriceStr = cols.get(2).text().replaceAll("[^0-9.]", "");

                        if (!buyPriceStr.isEmpty() && !sellPriceStr.isEmpty()) {
                            Double buyPrice = Double.parseDouble(buyPriceStr);
                            Double sellPrice = Double.parseDouble(sellPriceStr);

                            GoldPrice goldPrice = new GoldPrice();
                            goldPrice.setGoldType(goldType);
                            goldPrice.setBuyPrice(buyPrice);
                            goldPrice.setSellPrice(sellPrice);
                            goldPrice.setCompany("DOJI");
                            goldPrice.setCrawledAt(now);
                            goldPrice.setUpdatedAt(now);

                            goldPrices.add(goldPrice);
                            System.out.println("Parsed: " + goldType + " - " + buyPrice + " / " + sellPrice);
                        }
                    } catch (Exception e) {
                        // Skip lỗi parse
                    }
                }
            }

            // Nếu không crawl được, tạo dữ liệu mẫu để demo
            if (goldPrices.isEmpty()) {
                System.out.println("Không crawl được từ web, tạo dữ liệu mẫu...");
                goldPrices = createSampleData(now);
            }

            // Lưu vào database
            if (!goldPrices.isEmpty()) {
                goldPriceRepository.saveAll(goldPrices);
                System.out.println("Đã crawl và lưu " + goldPrices.size() + " giá vàng");
            }

        } catch (Exception e) {
            System.out.println("Lỗi khi crawl giá vàng: " + e.getMessage());
            // Tạo dữ liệu mẫu khi lỗi
            LocalDateTime now = LocalDateTime.now();
            goldPrices = createSampleData(now);

            if (!goldPrices.isEmpty()) {
                goldPriceRepository.saveAll(goldPrices);
                System.out.println("Đã tạo " + goldPrices.size() + " dữ liệu mẫu");
            }
        }

        return goldPrices;
    }

    // Tạo dữ liệu mẫu
    private List<GoldPrice> createSampleData(LocalDateTime now) {
        List<GoldPrice> goldPrices = new ArrayList<>();

        // SJC
        goldPrices.add(createGoldPrice("SJC 1L, 10L", 87500.0, 88200.0, "SJC", now));
        goldPrices.add(createGoldPrice("SJC 5c", 87450.0, 88150.0, "SJC", now));
        goldPrices.add(createGoldPrice("Nhẫn tròn trơn 9999", 86800.0, 87600.0, "SJC", now));

        // PNJ
        goldPrices.add(createGoldPrice("Vàng nhẫn 9999", 86700.0, 87500.0, "PNJ", now));
        goldPrices.add(createGoldPrice("Vàng SJC 1L", 87400.0, 88100.0, "PNJ", now));

        // DOJI
        goldPrices.add(createGoldPrice("Vàng miếng SJC", 87450.0, 88150.0, "DOJI", now));
        goldPrices.add(createGoldPrice("Nhẫn tròn trơn", 86750.0, 87550.0, "DOJI", now));

        return goldPrices;
    }

    private GoldPrice createGoldPrice(String type, Double buy, Double sell, String company, LocalDateTime time) {
        GoldPrice gp = new GoldPrice();
        gp.setGoldType(type);
        gp.setBuyPrice(buy);
        gp.setSellPrice(sell);
        gp.setCompany(company);
        gp.setCrawledAt(time);
        gp.setUpdatedAt(time);
        return gp;
    }

    // Lấy tất cả giá vàng từ database
    public List<GoldPrice> getAllGoldPrices() {
        return goldPriceRepository.findAllByOrderByCrawledAtDesc();
    }

    // Lấy giá vàng theo công ty
    public List<GoldPrice> getGoldPricesByCompany(String company) {
        return goldPriceRepository.findByCompanyOrderByCrawledAtDesc(company);
    }

    // Tự động crawl mỗi giờ (có thể điều chỉnh)
    @Scheduled(fixedRate = 3600000) // 1 giờ = 3600000ms
    public void scheduledCrawl() {
        System.out.println("Bắt đầu crawl giá vàng tự động...");
        crawlGoldPrices();
    }
}
