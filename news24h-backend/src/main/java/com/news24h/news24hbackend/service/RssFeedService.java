package com.news24h.news24hbackend.service;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RssFeedService {

    private final NewsService newsService;

    public RssFeedService(NewsService newsService) {
        this.newsService = newsService;
    }

    // Danh sách RSS: slug => URL
    private static final Map<String, String> RSS_FEEDS = new LinkedHashMap<>();

    static {
        RSS_FEEDS.put("trang-chu", "https://www.24h.com.vn/upload/rss/trangchu24h.rss");
        RSS_FEEDS.put("tin-tuc-trong-ngay", "https://www.24h.com.vn/upload/rss/tintuctrongngay.rss");
        RSS_FEEDS.put("bong-da", "https://www.24h.com.vn/upload/rss/bongda.rss");
        RSS_FEEDS.put("an-ninh-hinh-su", "https://www.24h.com.vn/upload/rss/anninhhinhsu.rss");
        RSS_FEEDS.put("thoi-trang", "https://www.24h.com.vn/upload/rss/thoitrang.rss");
        RSS_FEEDS.put("thoi-trang-hi-tech", "https://www.24h.com.vn/upload/rss/thoitranghitech.rss");
        RSS_FEEDS.put("tai-chinh-bat-dong-san", "https://www.24h.com.vn/upload/rss/taichinhbatdongsan.rss");
        RSS_FEEDS.put("am-thuc", "https://www.24h.com.vn/upload/rss/amthuc.rss");
        RSS_FEEDS.put("lam-dep", "https://www.24h.com.vn/upload/rss/lamdep.rss");
        RSS_FEEDS.put("phim", "https://www.24h.com.vn/upload/rss/phim.rss");
        RSS_FEEDS.put("giao-duc-du-hoc", "https://www.24h.com.vn/upload/rss/giaoducduhoc.rss");
        RSS_FEEDS.put("ban-tre-cuoc-song", "https://www.24h.com.vn/upload/rss/bantrecuocsong.rss");
        RSS_FEEDS.put("ca-nhac-mtv", "https://www.24h.com.vn/upload/rss/canhacmtv.rss");
        RSS_FEEDS.put("the-thao", "https://www.24h.com.vn/upload/rss/thethao.rss");
        RSS_FEEDS.put("phi-thuong-ky-quac", "https://www.24h.com.vn/upload/rss/phithuongkyquac.rss");
        RSS_FEEDS.put("cong-nghe-thong-tin", "https://www.24h.com.vn/upload/rss/congnghethongtin.rss");
        RSS_FEEDS.put("oto", "https://www.24h.com.vn/upload/rss/oto.rss");
        RSS_FEEDS.put("thi-truong-tieu-dung", "https://www.24h.com.vn/upload/rss/thitruongtieudung.rss");
        RSS_FEEDS.put("du-lich", "https://www.24h.com.vn/upload/rss/dulich.rss");
        RSS_FEEDS.put("suc-khoe-doi-song", "https://www.24h.com.vn/upload/rss/suckhoedoisong.rss");
    }

    /**
     * Chạy 1 lần khi khởi động và sau đó mỗi 10 phút
     */
    @PostConstruct
    @Scheduled(fixedRate = 600_000) // 10 phút
    public void fetchAllRss() {
        RSS_FEEDS.forEach(this::fetchRss);
    }

    private void fetchRss(String slug, String rssUrl) {
        try {
            Document doc = Jsoup.connect(rssUrl)
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements items = doc.select("item");

            items.forEach(item -> {
                String title = item.selectFirst("title") != null
                        ? item.selectFirst("title").text()
                        : "";
                String link = item.selectFirst("link") != null
                        ? item.selectFirst("link").text()
                        : "";

                String descriptionHtml = item.selectFirst("description") != null
                        ? item.selectFirst("description").text()
                        : "";

                String thumbnail = extractThumbnail(descriptionHtml);

                // Loại bỏ tag HTML để hiển thị preview
                String description = Jsoup.parse(descriptionHtml).text();

                newsService.createOrUpdateFromRss(
                        title,
                        description,
                        thumbnail,
                        slug,
                        link,
                        Instant.now()
                );
            });

            System.out.println("RSS loaded: " + slug);

        } catch (Exception e) {
            System.err.println("RSS error (" + slug + "): " + e.getMessage());
        }
    }

    private String extractThumbnail(String descHtml) {
        try {
            Document doc = Jsoup.parse(descHtml);
            String url = doc.select("img").attr("src");
            return (url != null && !url.isEmpty())
                    ? url
                    : "https://picsum.photos/800/400";
        } catch (Exception e) {
            return "https://picsum.photos/800/400";
        }
    }
}
