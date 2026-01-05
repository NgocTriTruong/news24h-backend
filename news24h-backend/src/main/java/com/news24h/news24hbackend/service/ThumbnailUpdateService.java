package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThumbnailUpdateService {

    private final NewsArticleRepository newsRepository;

    public ThumbnailUpdateService(NewsArticleRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * Update thumbnails cho các bài viết đang dùng placeholder
     * Chạy riêng để không block main crawl process
     */
    public int updatePlaceholderThumbnails() {
        int updated = 0;

        try {
            // Tìm tất cả bài viết có thumbnail là picsum.photos
            List<NewsArticle> articlesWithPlaceholder = newsRepository
                    .findAll()
                    .stream()
                    .filter(a -> a.getThumbnail() != null && a.getThumbnail().contains("picsum.photos"))
                    .toList();

            System.out.println("Tìm thấy " + articlesWithPlaceholder.size() + " bài viết cần update thumbnail");

            for (NewsArticle article : articlesWithPlaceholder) {
                try {
                    String sourceUrl = article.getSourceUrl();
                    if (sourceUrl == null || sourceUrl.isEmpty()) {
                        continue;
                    }

                    System.out.println("Đang update thumbnail cho: " + article.getTitle());

                    Document doc = Jsoup.connect(sourceUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(15000)
                            .get();

                    String thumbnail = null;

                    // Thử og:image trước
                    Element ogImage = doc.selectFirst("meta[property=og:image]");
                    if (ogImage != null && ogImage.hasAttr("content")) {
                        thumbnail = ogImage.attr("content");
                    }

                    // Nếu không có og:image, thử img trong article
                    if (thumbnail == null || thumbnail.isEmpty()) {
                        Element articleImg = doc.selectFirst("article img, .article-content img, .news-content img");
                        if (articleImg != null && articleImg.hasAttr("src")) {
                            thumbnail = articleImg.attr("abs:src");
                        }
                    }

                    // Nếu không có trong article, thử img đầu tiên trong body
                    if (thumbnail == null || thumbnail.isEmpty()) {
                        Element firstImg = doc.selectFirst("body img");
                        if (firstImg != null && firstImg.hasAttr("src")) {
                            thumbnail = firstImg.attr("abs:src");
                        }
                    }

                    // Chỉ update nếu tìm được thumbnail thật
                    if (thumbnail != null && !thumbnail.isEmpty() && !thumbnail.contains("picsum.photos")) {
                        article.setThumbnail(thumbnail);
                        newsRepository.save(article);
                        updated++;
                        System.out.println("✓ Updated thumbnail: " + thumbnail);
                    } else {
                        System.out.println("✗ Không tìm thấy thumbnail cho: " + article.getTitle());
                    }

                    // Sleep để tránh bị block
                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.out.println("Lỗi khi update thumbnail cho bài viết " + article.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("Đã update " + updated + "/" + articlesWithPlaceholder.size() + " thumbnails");

        } catch (Exception e) {
            System.out.println("Lỗi trong quá trình update thumbnails: " + e.getMessage());
        }

        return updated;
    }

    /**
     * Update thumbnail cho một bài viết cụ thể
     */
    public boolean updateThumbnailForArticle(String articleId) {
        try {
            NewsArticle article = newsRepository.findById(articleId).orElse(null);
            if (article == null) {
                return false;
            }

            String sourceUrl = article.getSourceUrl();
            if (sourceUrl == null || sourceUrl.isEmpty()) {
                return false;
            }

            Document doc = Jsoup.connect(sourceUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            String thumbnail = null;

            // Thử og:image
            Element ogImage = doc.selectFirst("meta[property=og:image]");
            if (ogImage != null && ogImage.hasAttr("content")) {
                thumbnail = ogImage.attr("content");
            }

            // Fallback: img trong article
            if (thumbnail == null || thumbnail.isEmpty()) {
                Element articleImg = doc.selectFirst("article img, .article-content img");
                if (articleImg != null) {
                    thumbnail = articleImg.attr("abs:src");
                }
            }

            if (thumbnail != null && !thumbnail.isEmpty()) {
                article.setThumbnail(thumbnail);
                newsRepository.save(article);
                return true;
            }

        } catch (Exception e) {
            System.out.println("Lỗi update thumbnail: " + e.getMessage());
        }

        return false;
    }
}
