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

@Service
public class StartupNewsService {

    private final NewsArticleRepository newsRepository;
    private final ArticleCrawlerService crawlerService;
    private final ThumbnailUpdateService thumbnailUpdateService;

    public StartupNewsService(NewsArticleRepository newsRepository,
                              ArticleCrawlerService crawlerService,
                              ThumbnailUpdateService thumbnailUpdateService) {
        this.newsRepository = newsRepository;
        this.crawlerService = crawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;
    }

    public List<NewsArticle> crawlStartupNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            String url = "https://www.24h.com.vn/khoi-nghiep-c826.html";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            Elements newsLinks = doc.select("h3 a, h2 a, .cate-24h-foot-home-list-item a, article a");
            System.out.println("Tìm thấy " + newsLinks.size() + " links từ " + url);

            for (Element link : newsLinks) {
                try {
                    String title = link.text().trim();
                    String href = link.attr("abs:href");

                    // Filter bỏ tin bóng đá
                    String titleLower = title.toLowerCase();
                    boolean isSportsNews = titleLower.contains("bóng đá") || titleLower.contains("bong da")
                            || titleLower.contains("ngoại hạng anh") || titleLower.contains("premier league")
                            || titleLower.contains("champions league") || titleLower.contains("cup c1");

                    if (!title.isEmpty() && title.length() > 10 &&
                            href.contains("24h.com.vn") &&
                            !href.contains("#") &&
                            !href.contains("javascript") &&
                            !isSportsNews) {

                        if (newsRepository.findBySourceUrl(href).isEmpty()) {
                            String thumbnail = null;
                            try {
                                // Tìm ảnh trong parent element
                                Element parent = link.parent();
                                while (parent != null && thumbnail == null) {
                                    Element img = parent.selectFirst("img");
                                    if (img != null) {
                                        if (img.hasAttr("data-src")) {
                                            thumbnail = img.attr("abs:data-src");
                                        } else if (img.hasAttr("src")) {
                                            thumbnail = img.attr("abs:src");
                                        }
                                    }
                                    if (thumbnail == null || thumbnail.isEmpty() || thumbnail.contains("1x1")) {
                                        parent = parent.parent();
                                        if (parent != null && parent.tagName().equals("body")) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore
                            }

                            if (thumbnail == null || thumbnail.isEmpty() || thumbnail.contains("1x1")) {
                                thumbnail = "https://picsum.photos/800/400";
                            }

                            NewsArticle article = new NewsArticle();
                            article.setTitle(title);
                            article.setDescription("Tin tức Khởi nghiệp");
                            article.setThumbnail(thumbnail);
                            article.setCategory("khoi-nghiep");
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
            }

        } catch (Exception e) {
            System.out.println("Lỗi khi crawl tin khởi nghiệp: " + e.getMessage());
        }

        if (!articles.isEmpty()) {
            System.out.println("Bắt đầu update thumbnails...");
            int updatedCount = thumbnailUpdateService.updatePlaceholderThumbnails();
            System.out.println("Đã update " + updatedCount + " thumbnails");
        }

        return articles;
    }

    @Scheduled(fixedRate = 3600000) // Chạy mỗi 1 giờ
    public void scheduledCrawlStartupNews() {
        System.out.println("Đang crawl tin khởi nghiệp...");
        crawlStartupNews();
    }
}
