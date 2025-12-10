package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ArticleCrawlerService {

    private final NewsArticleRepository newsRepo;

    // Inject repository để còn lưu nội dung vào DB
    public ArticleCrawlerService(NewsArticleRepository newsRepo) {
        this.newsRepo = newsRepo;
    }

    // Crawl toàn bộ nội dung bài viết từ link 24h.com.vn
    public String crawlHtmlFrom24h(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            Element content = doc.selectFirst("article.cate-24h-foot-arti-deta-info");

            if (content == null) {
                System.out.println("Không tìm thấy nội dung chính cho URL: " + url);
                return "";
            }

            // Xóa bớt quảng cáo / script / iframe / các block không cần thiết
            content.select("script, iframe, .ads, .advertisement, .banner, .social").remove();

            // Xóa block bài viết liên quan (bv-lq)
            content.select(".bv-lq").remove();

            // Xóa toàn bộ link trỏ về 24h.com.vn nhưng giữ text
            content.select("a[href*='24h.com.vn']").forEach(a -> a.unwrap());

            // FIX lazy-load image
            content.select("img").forEach(img -> {

                if (img.hasAttr("data-original") && !img.attr("data-original").isBlank()) {
                    img.attr("src", img.attr("data-original"));
                } else if (img.hasAttr("data-src") && !img.attr("data-src").isBlank()) {
                    img.attr("src", img.attr("data-src"));
                } else if (img.hasAttr("data-img") && !img.attr("data-img").isBlank()) {
                    img.attr("src", img.attr("data-img"));
                }

                if (img.attr("src").startsWith("data:image")) {
                    img.removeAttr("src");
                }
            });

            // Trả về HTML nội dung
            return content.html();

        } catch (Exception e) {
            System.out.println("Lỗi crawl nội dung: " + e.getMessage());
            return "";
        }
    }

    // Crawl từ sourceUrl rồi lưu content HTML vào DB.
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
