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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoldNewsService {

    private final NewsArticleRepository newsRepository;
    private final ArticleCrawlerService crawlerService;
    private final ThumbnailUpdateService thumbnailUpdateService;

    public GoldNewsService(NewsArticleRepository newsRepository,
                           ArticleCrawlerService crawlerService,
                           ThumbnailUpdateService thumbnailUpdateService) {
        this.newsRepository = newsRepository;
        this.crawlerService = crawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;
    }

    /**
     * Crawl tin tức từ giavang.net
     */
    public List<NewsArticle> crawlGoldNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            // Crawl từ nhiều nguồn
            String[] urls = {
                    "https://giavang.net/",
                    "https://giavang.net/tin-tuc",
                    "https://www.24h.com.vn/gia-vang-hom-nay-c425.html"
            };

            for (String url : urls) {
                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(15000)
                            .get();

                    // Selector khác nhau cho từng nguồn
                    Elements newsLinks;
                    if (url.contains("24h.com.vn")) {
                        // Với 24h, lấy tất cả links trong trang
                        newsLinks = doc.select("a[href*=c161a], a[href*=kinh-doanh]");
                    } else {
                        // Với giavang.net, dùng selector như cũ
                        newsLinks = doc.select("a[href*=tin-tuc], a[href*=bai-viet]");

                        if (newsLinks.isEmpty()) {
                            newsLinks = doc.select("article a, .news-item a, .post-item a");
                        }

                        if (newsLinks.isEmpty()) {
                            newsLinks = doc.select("h2 a, h3 a, h4 a");
                        }
                    }

                    System.out.println("Tìm thấy " + newsLinks.size() + " links từ " + url);

                    for (Element link : newsLinks) {
                        try {
                            String title = link.text().trim();
                            String href = link.attr("abs:href");

                            // Lọc NGAY khi crawl - CHỈ lấy tin có chữ "Vàng" hoặc "vàng"
                            String titleLower = title.toLowerCase();
                            boolean hasVangKeyword = title.contains("Vàng") || title.contains("vàng") || titleLower.contains(" gold");

                            // Loại bỏ các tin về vàng mã, Goldman Sachs
                            boolean isInvalidGold = titleLower.contains("vàng mã") || titleLower.contains("goldman");

                            // Lọc chỉ lấy link có nội dung hợp lệ VÀ CÓ CHỮ VÀNG (không phải vàng mã/goldman)
                            if (!title.isEmpty() && title.length() > 10 &&
                                    (href.contains("giavang.net") || href.contains("24h.com.vn")) &&
                                    !href.contains("#") &&
                                    !href.contains("javascript") &&
                                    hasVangKeyword && !isInvalidGold) { // CHỈ LẤY TIN CÓ CHỮ VÀNG THẬT

                                // Kiểm tra xem bài viết đã tồn tại chưa
                                if (newsRepository.findBySourceUrl(href).isEmpty()) {
                                    // Thử lấy thumbnail từ parent element trước (nhanh hơn)
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

                                    // Nếu không tìm thấy thumbnail trong listing, sẽ dùng placeholder
                                    // và update sau bằng endpoint riêng
                                    if (thumbnail == null || thumbnail.isEmpty()) {
                                        thumbnail = "https://picsum.photos/800/400";
                                    }

                                    NewsArticle article = new NewsArticle();
                                    article.setTitle(title);
                                    article.setDescription("Tin tức về giá vàng bạc thị trường");
                                    article.setThumbnail(thumbnail);
                                    article.setCategory("gia-vang");
                                    article.setSourceUrl(href);
                                    article.setPublishedAt(Instant.now());
                                    article.setFeatured(false);

                                    newsRepository.save(article);
                                    articles.add(article);

                                    System.out.println("Đã lưu: " + title);

                                    // Crawl content ngay sau khi lưu
                                    try {
                                        crawlerService.crawlContentForArticle(article.getId());
                                    } catch (Exception e) {
                                        System.out.println("Lỗi crawl content cho " + title + ": " + e.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Skip lỗi
                        }

                        // Giới hạn số lượng
                        if (articles.size() >= 15) {
                            break;
                        }
                    }

                    if (articles.size() >= 15) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi crawl " + url + ": " + e.getMessage());
                }
            }

            System.out.println("Đã crawl " + articles.size() + " tin tức mới về giá vàng");

            // Tự động update thumbnails cho tin vừa crawl
            if (!articles.isEmpty()) {
                System.out.println("Đang update thumbnails cho tin mới...");
                int updated = thumbnailUpdateService.updatePlaceholderThumbnails();
                System.out.println("Đã update " + updated + " thumbnails");
            }

        } catch (Exception e) {
            System.out.println("Lỗi khi crawl tin tức giá vàng: " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

    /**
     * Crawl lại content cho các tin đã có
     */
    public int refreshContentForExistingNews() {
        // Lấy tất cả tin giá vàng chưa có content - lấy tối đa 100 tin
        List<NewsArticle> articles = newsRepository.findByCategoryOrderByPublishedAtDesc(
                        "gia-vang",
                        org.springframework.data.domain.PageRequest.of(0, 100)
                )
                .stream()
                .filter(article -> article.getContent() == null || article.getContent().isEmpty())
                .collect(Collectors.toList());

        System.out.println("Tìm thấy " + articles.size() + " tin tức chưa có content");

        int count = 0;
        for (NewsArticle article : articles) {
            try {
                System.out.println("Đang crawl content cho: " + article.getTitle());
                crawlerService.crawlContentForArticle(article.getId());
                count++;
                Thread.sleep(1000); // Delay 1s giữa các request
            } catch (Exception e) {
                System.out.println("Lỗi khi crawl content cho article " + article.getId() + ": " + e.getMessage());
            }
        }

        return count;
    }

    /**
     * Xóa các tin tức không liên quan đến giá vàng
     */
    public int cleanupNonGoldNews() {
        List<NewsArticle> articles = newsRepository.findByCategoryOrderByPublishedAtDesc(
                "gia-vang",
                org.springframework.data.domain.PageRequest.of(0, 200)
        ).getContent();

        int count = 0;
        for (NewsArticle article : articles) {
            String title = article.getTitle();
            String titleLower = title.toLowerCase();
            String sourceUrl = article.getSourceUrl() != null ? article.getSourceUrl() : "";

            // CHỈ GIỮ tin có chữ "Vàng" hoặc "vàng" hoặc " gold" (có khoảng trắng)
            boolean hasVangKeyword = title.contains("Vàng") || title.contains("vàng") ||
                    titleLower.contains(" gold");

            // Loại bỏ tin về vàng mã, Goldman Sachs
            boolean isInvalidGold = titleLower.contains("vàng mã") || titleLower.contains("goldman");

            // Xóa nếu: không có chữ vàng HOẶC có vàng mã/goldman HOẶC không từ nguồn hợp lệ
            if (!hasVangKeyword || isInvalidGold || (!sourceUrl.contains("giavang.net") && !sourceUrl.contains("24h.com.vn"))) {
                System.out.println("Xóa tin không liên quan: " + article.getTitle());
                newsRepository.delete(article);
                count++;
            }
        }

        return count;
    }

    /**
     * Tự động crawl tin tức mỗi 2 giờ
     */
    @Scheduled(fixedRate = 7200000) // 2 giờ = 7200000ms
    public void scheduledCrawl() {
        System.out.println("Bắt đầu crawl tin tức giá vàng tự động...");
        crawlGoldNews();
    }
}
