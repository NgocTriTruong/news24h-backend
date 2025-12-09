package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Service chuyên crawl nội dung HTML từ bài viết 24h.com.vn
 * để nhúng vào website của bạn.
 */
@Service
public class ArticleCrawlerService {

    private final NewsArticleRepository newsRepo;

    // Inject repository để còn lưu nội dung vào DB
    public ArticleCrawlerService(NewsArticleRepository newsRepo) {
        this.newsRepo = newsRepo;
    }

    /**
     * Crawl toàn bộ nội dung bài viết từ link 24h.com.vn
     * @param url link gốc của bài viết
     * @return chuỗi HTML (inner HTML) của phần nội dung chính
     */
    public String crawlHtmlFrom24h(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            // Tìm phần nội dung chính. Cần tùy chỉnh nếu 24h đổi layout.
            Element content =
                    doc.selectFirst("#article_id") != null
                            ? doc.selectFirst("#article_id")
                            : doc.selectFirst("article") != null
                            ? doc.selectFirst("article")
                            : doc.selectFirst(".text-conent");

            if (content == null) {
                System.out.println("Không tìm thấy nội dung chính cho URL: " + url);
                return "";
            }

            // Xóa bớt quảng cáo / script / iframe / các block không cần thiết
            content.select("script, iframe, .ads, .advertisement, .banner, .social").remove();

            // Trả về HTML nội dung
            return content.html();

        } catch (Exception e) {
            System.out.println("Lỗi crawl nội dung: " + e.getMessage());
            return "";
        }
    }

    /**
     * Hàm được NewsController gọi:
     * POST /api/news/{id}/crawl
     * -> Crawl từ sourceUrl rồi lưu content HTML vào DB.
     */
    public void crawlContentForArticle(String articleId) {
        // Lấy bài viết từ DB
        NewsArticle article = newsRepo.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (article.getSourceUrl() == null || article.getSourceUrl().isBlank()) {
            System.out.println("Article không có sourceUrl để crawl");
            return;
        }

        // Dùng lại hàm cũ bạn đã viết
        String html = crawlHtmlFrom24h(article.getSourceUrl());

        if (html != null && !html.isBlank()) {
            article.setContent(html);
            newsRepo.save(article);
            System.out.println("Đã crawl & lưu nội dung cho bài: " + articleId);
        } else {
            System.out.println("Không crawl được nội dung cho bài: " + articleId);
        }
    }
}
