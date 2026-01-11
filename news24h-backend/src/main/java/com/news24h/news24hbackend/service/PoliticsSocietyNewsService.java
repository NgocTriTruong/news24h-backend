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
public class PoliticsSocietyNewsService {

    private final NewsArticleRepository newsRepository;
    private final ArticleCrawlerService crawlerService;
    private final ThumbnailUpdateService thumbnailUpdateService;

    public PoliticsSocietyNewsService(NewsArticleRepository newsRepository,
                                      ArticleCrawlerService crawlerService,
                                      ThumbnailUpdateService thumbnailUpdateService) {
        this.newsRepository = newsRepository;
        this.crawlerService = crawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;
    }

    /**
     * Crawl tin tức Chính trị - Xã hội
     */
    public List<NewsArticle> crawlPoliticsSocietyNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            String url = "https://www.24h.com.vn/chinh-tri-xa-hoi-c981.html";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            // Lấy tất cả link bài viết
            Elements newsLinks = doc.select("h3 a, h2 a, .cate-24h-foot-home-list-item a, article a");

            System.out.println("Tìm thấy " + newsLinks.size() + " links từ " + url);

            for (Element link : newsLinks) {
                try {
                    String title = link.text().trim();
                    String href = link.attr("abs:href");

                    if (!title.isEmpty() && title.length() > 10 &&
                            href.contains("24h.com.vn") &&
                            !href.contains("#") &&
                            !href.contains("javascript")) {

                        // Kiểm tra đã tồn tại chưa
                        if (newsRepository.findBySourceUrl(href).isEmpty()) {
                            // Thử lấy thumbnail từ parent element
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

                            // Nếu không tìm thấy thumbnail, dùng placeholder
                            if (thumbnail == null || thumbnail.isEmpty()) {
                                thumbnail = "https://picsum.photos/800/400";
                            }

                            NewsArticle article = new NewsArticle();
                            article.setTitle(title);
                            article.setDescription("Tin tức Chính trị - Xã hội");
                            article.setThumbnail(thumbnail);
                            article.setCategory("chinh-tri-xa-hoi");
                            article.setSourceUrl(href);
                            article.setPublishedAt(Instant.now());
                            article.setFeatured(false);

                            newsRepository.save(article);
                            articles.add(article);

                            System.out.println("Đã lưu: " + title);

                            // Crawl content
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

                if (articles.size() >= 15) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi crawl chinh-tri-xa-hoi: " + e.getMessage());
            e.printStackTrace();
        }

        // Auto-update thumbnails sau khi crawl xong
        if (!articles.isEmpty()) {
            System.out.println("Bắt đầu update thumbnails cho " + articles.size() + " tin mới...");
            int updatedCount = thumbnailUpdateService.updatePlaceholderThumbnails();
            System.out.println("Đã update " + updatedCount + " thumbnails");
        }

        return articles;
    }

    @Scheduled(fixedRate = 43200000, initialDelay = 70000)
    public void scheduledCrawl() {
        System.out.println("Bắt đầu crawl tin Chính trị - Xã hội tự động...");
        crawlPoliticsSocietyNews();
    }
}
