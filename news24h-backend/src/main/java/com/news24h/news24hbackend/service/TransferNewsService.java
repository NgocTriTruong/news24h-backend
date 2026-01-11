package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Service
public class TransferNewsService {

    private final NewsArticleRepository newsRepository;
    private final ArticleCrawlerService crawlerService;
    private final ThumbnailUpdateService thumbnailUpdateService;

    public TransferNewsService(NewsArticleRepository newsRepository,
                               ArticleCrawlerService crawlerService,
                               ThumbnailUpdateService thumbnailUpdateService) {
        this.newsRepository = newsRepository;
        this.crawlerService = crawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;
    }

    public List<NewsArticle> crawlTransferNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            String url = "https://www.24h.com.vn/tin-chuyen-nhuong-c53.html";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            Elements newsLinks = doc.select("h3 a, h2 a, .cate-24h-foot-home-list-item a, article a");
            System.out.println("Tìm thấy " + newsLinks.size() + " links từ " + url);

            // Danh sách từ khóa liên quan bóng đá/chuyển nhượng
            String[] footballKeywords = {
                    "bóng đá", "cầu thủ", "CLB", "hợp đồng", "chuyển nhượng", "football", "transfer", "giải", "HLV", "Cristiano Ronaldo", "Messi", "Real Madrid", "MU", "Man United", "Chelsea", "Arsenal", "Barca", "Barcelona", "Liverpool", "PSG", "Juventus", "Inter", "Milan", "Bayern", "Dortmund", "Premier League", "La Liga", "Serie A", "Bundesliga", "Ligue 1"
            };

            for (Element link : newsLinks) {
                try {
                    String title = link.text().trim();
                    String href = link.attr("abs:href");

                    // Lọc chỉ lấy tin có từ khóa bóng đá/chuyển nhượng
                    boolean isFootballTransfer = false;
                    String lowerTitle = title.toLowerCase();
                    for (String kw : footballKeywords) {
                        if (lowerTitle.contains(kw.toLowerCase())) {
                            isFootballTransfer = true;
                            break;
                        }
                    }
                    if (!isFootballTransfer) continue;

                    if (!title.isEmpty() && title.length() > 10 &&
                            href.contains("24h.com.vn") &&
                            !href.contains("#") &&
                            !href.contains("javascript")) {

                        if (newsRepository.findBySourceUrl(href).isEmpty()) {
                            String thumbnail = null;
                            try {
                                Element parent = link.parent();
                                if (parent != null) {
                                    Element imgInParent = parent.selectFirst("img");
                                    if (imgInParent != null && imgInParent.hasAttr("src")) {
                                        thumbnail = imgInParent.attr("abs:src");
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore
                            }

                            if (thumbnail == null || thumbnail.isEmpty()) {
                                thumbnail = "https://picsum.photos/800/400";
                            }

                            NewsArticle article = new NewsArticle();
                            article.setTitle(title);
                            article.setDescription("Tin chuyển nhượng");
                            article.setThumbnail(thumbnail);
                            article.setCategory("chuyen-nhuong");
                            article.setSourceUrl(href);
                            article.setPublishedAt(Instant.now());
                            article.setFeatured(false);

                            newsRepository.save(article);
                            articles.add(article);
                            System.out.println("Đã lưu: " + title);

                            try {
                                crawlerService.crawlContentForArticle(article.getId());
                            } catch (Exception e) {
                                System.out.println("Lỗi crawl content: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip lỗi
                }

                if (articles.size() >= 25) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi crawl chuyen-nhuong: " + e.getMessage());
        }

        if (!articles.isEmpty()) {
            System.out.println("Bắt đầu update thumbnails...");
            int updatedCount = thumbnailUpdateService.updatePlaceholderThumbnails();
            System.out.println("Đã update " + updatedCount + " thumbnails");
        }

        return articles;
    }

    @Scheduled(fixedRate = 43200000, initialDelay = 35000)
    public void scheduledCrawl() {
        System.out.println("Bắt đầu crawl tin Chuyển nhượng tự động...");
        crawlTransferNews();
    }

}
