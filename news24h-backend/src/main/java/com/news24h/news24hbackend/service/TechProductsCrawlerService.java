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
import java.util.*;

@Service
public class TechProductsCrawlerService {

    private final NewsArticleRepository newsArticleRepository;
    private final ArticleCrawlerService articleCrawlerService;
    private final ThumbnailUpdateService thumbnailUpdateService;

    // Record-based category configuration
    public record CategoryConfig(
            String slug,
            String url,
            String description,
            int maxItems,
            long initialDelayMs
    ) {}

    private final Map<String, CategoryConfig> configBySlug = new LinkedHashMap<>();

    public TechProductsCrawlerService(
            NewsArticleRepository newsArticleRepository,
            ArticleCrawlerService articleCrawlerService,
            ThumbnailUpdateService thumbnailUpdateService) {
        this.newsArticleRepository = newsArticleRepository;
        this.articleCrawlerService = articleCrawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;

        // Initialize category configurations - ƯU TIÊN CRAWL TRƯỚC
        configBySlug.put("diem-nong", new CategoryConfig(
                "diem-nong",
                "https://www.24h.com.vn/diem-nong-c704.html",
                "Điểm nóng",
                5,
                1000
        ));
        configBySlug.put("quan-su", new CategoryConfig(
                "quan-su",
                "https://www.24h.com.vn/quan-su-c705.html",
                "Quân sự",
                5,
                2000
        ));
        configBySlug.put("theo-dong-lich-su", new CategoryConfig(
                "theo-dong-lich-su",
                "https://www.24h.com.vn/theo-dong-lich-su-c706.html",
                "Theo dòng lịch sử",
                5,
                3000
        ));
        configBySlug.put("dien-thoai", new CategoryConfig(
                "dien-thoai",
                "https://www.24h.com.vn/dien-thoai-c419.html",
                "Điện thoại",
                5,
                5000
        ));
        configBySlug.put("laptop-gia-re", new CategoryConfig(
                "laptop-gia-re",
                "https://www.24h.com.vn/laptop-gia-re-c451.html",
                "Laptop giá rẻ",
                5,
                15000
        ));
        configBySlug.put("may-tinh-de-ban", new CategoryConfig(
                "may-tinh-de-ban",
                "https://www.24h.com.vn/may-tinh-de-ban-c290.html",
                "Máy tính để bàn",
                5,
                25000
        ));
        configBySlug.put("may-tinh-bang", new CategoryConfig(
                "may-tinh-bang",
                "https://www.24h.com.vn/may-tinh-bang-c699.html",
                "Máy tính bảng",
                5,
                35000
        ));
        configBySlug.put("tin-tuc-cong-nghe", new CategoryConfig(
                "tin-tuc-cong-nghe",
                "https://www.24h.com.vn/tin-tuc-cong-nghe-c453.html",
                "Tin tức công nghệ",
                5,
                45000
        ));
        configBySlug.put("cac-san-pham-khac", new CategoryConfig(
                "cac-san-pham-khac",
                "https://www.24h.com.vn/cac-san-pham-khac-c423.html",
                "Các sản phẩm khác",
                5,
                55000
        ));
    }

    // Main crawl method for category by slug
    public List<NewsArticle> crawlCategory(String slug) {
        CategoryConfig config = configBySlug.get(slug);
        if (config == null) {
            System.out.println(" Không tìm thấy cấu hình cho danh mục: " + slug);
            return new ArrayList<>();
        }
        return crawl(config);
    }

    // Unified crawl logic for all tech product categories
    private List<NewsArticle> crawl(CategoryConfig config) {
        List<NewsArticle> articles = new ArrayList<>();
        System.out.println(" Đang crawl: " + config.description + " (" + config.slug + ")");

        try {
            Document doc = Jsoup.connect(config.url)
                    .timeout(15000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // Selector tổng quát như HealthNewsCrawlerService
            Elements links = doc.select("h3 a, h2 a, .cate-24h-foot-home-list-item a, article a");
            System.out.println("🔍 Tìm thấy " + links.size() + " links từ " + config.url);

            Set<String> seen = new HashSet<>();
            int count = 0;
            for (Element link : links) {
                if (count >= config.maxItems) break;

                // Check for ads/sponsored
                if (isAdOrSponsored(link)) continue;

                String sourceUrl = link.attr("href");
                if (sourceUrl.isEmpty() || sourceUrl.startsWith("javascript") || sourceUrl.startsWith("#")) continue;
                if (!sourceUrl.startsWith("http")) {
                    sourceUrl = "https://www.24h.com.vn" + (sourceUrl.startsWith("/") ? sourceUrl : "/" + sourceUrl);
                }
                // Only accept article URLs: ...-cNNNaNNN.html
                if (!sourceUrl.matches(".+-c\\d+a\\d+\\.html$")) continue;
                // Deduplicate
                if (!seen.add(sourceUrl)) continue;

                // Check if article already exists
                if (newsArticleRepository.findBySourceUrl(sourceUrl).isPresent()) {
                    continue;
                }

                try {
                    Document articleDoc = Jsoup.connect(sourceUrl)
                            .timeout(15000)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .get();

                    String title = articleDoc.select("h1.title-page").text();
                    if (title.isEmpty()) {
                        title = articleDoc.select("meta[property=og:title]").attr("content");
                    }

                    String description = articleDoc.select("meta[property=og:description]").attr("content");
                    if (description.isEmpty()) {
                        description = articleDoc.select("p.description-page").text();
                    }

                    // Extract thumbnail with robust handling
                    String thumbnail = extractThumbnail(articleDoc);

                    String content = articleDoc.select("div.article-content").html();
                    if (content.isEmpty()) {
                        content = articleDoc.select("div.article-body").html();
                    }

                    if (!title.isEmpty() && !description.isEmpty()) {
                        NewsArticle article = new NewsArticle();
                        article.setTitle(title);
                        article.setDescription(description);
                        article.setContent(content);
                        article.setThumbnail(thumbnail);
                        article.setCategory(config.slug);
                        article.setSourceUrl(sourceUrl);
                        article.setPublishedAt(Instant.now());

                        newsArticleRepository.save(article);
                        articles.add(article);
                        count++;

                        // TẠM TẮT CRAWL FULL CONTENT ĐỂ TEST NHANH
                        // articleCrawlerService.crawlContentForArticle(article.getId());
                    }
                } catch (Exception e) {
                    System.out.println(" Lỗi khi crawl: " + sourceUrl + " - " + e.getMessage());
                }
            }

            System.out.println(" Đã crawl " + count + " tin từ " + config.description);
        } catch (Exception e) {
            System.out.println(" Lỗi crawl " + config.description + ": " + e.getMessage());
        }

        return articles;
    }

    // Extract thumbnail with proper handling of srcset, data-src, and logo filtering
    private String extractThumbnail(Document doc) {
        // Try og:image first
        String ogImage = doc.select("meta[property=og:image]").attr("content");
        if (!ogImage.isEmpty() && isValidThumbnail(ogImage)) {
            return ogImage;
        }

        // Search for article images in content
        Elements containers = doc.select("div.article-content, div.article-body, article");
        for (Element container : containers) {
            Elements imgs = container.select("img");
            for (Element img : imgs) {
                if (isAdOrSponsored(img)) continue;

                // Try srcset first
                String srcset = img.attr("srcset");
                if (!srcset.isEmpty()) {
                    String[] srcArray = srcset.split(",");
                    if (srcArray.length > 0) {
                        String firstSrc = srcArray[0].trim().split("\\s+")[0];
                        if (isValidThumbnail(firstSrc) && !isLogoImage(firstSrc)) {
                            return resolveAbsoluteUrl(firstSrc);
                        }
                    }
                }

                // Try data-src
                String dataSrc = img.attr("data-src");
                if (!dataSrc.isEmpty() && isValidThumbnail(dataSrc) && !isLogoImage(dataSrc)) {
                    return resolveAbsoluteUrl(dataSrc);
                }

                // Try data-original
                String dataOriginal = img.attr("data-original");
                if (!dataOriginal.isEmpty() && isValidThumbnail(dataOriginal) && !isLogoImage(dataOriginal)) {
                    return resolveAbsoluteUrl(dataOriginal);
                }

                // Try src
                String src = img.attr("src");
                if (!src.isEmpty() && isValidThumbnail(src) && !isLogoImage(src)) {
                    return resolveAbsoluteUrl(src);
                }
            }
        }

        return "https://via.placeholder.com/500x300?text=No+Image";
    }

    // Check if thumbnail URL is valid
    private boolean isValidThumbnail(String url) {
        if (url == null || url.isEmpty()) return false;
        if (url.startsWith("data:image")) return false; // Reject data URIs
        if (url.contains("1x1") || url.contains("gif")) return false; // Reject 1x1 pixels
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("//")) {
            return false;
        }
        return true;
    }

    // Check if image is a logo or favicon
    private boolean isLogoImage(String url) {
        if (url == null) return false;
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains("logo") ||
                lowerUrl.contains("favicon") ||
                lowerUrl.contains("svg") ||
                lowerUrl.contains("watermark");
    }

    // Resolve relative URLs to absolute
    private String resolveAbsoluteUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("/")) {
            return "https://www.24h.com.vn" + url;
        }
        return "https://www.24h.com.vn/" + url;
    }

    // Check if element or parent is ad/sponsored
    private boolean isAdOrSponsored(Element element) {
        Element current = element;
        for (int i = 0; i < 5; i++) {
            if (current == null) break;

            String classes = current.className().toLowerCase();
            String id = current.id().toLowerCase();

            if (classes.contains("ad") || classes.contains("sponsored") || classes.contains("promo") ||
                    id.contains("ad") || id.contains("sponsored") || id.contains("promo")) {
                return true;
            }

            if (current.hasAttr("data-ad") || current.hasAttr("data-adslot")) {
                return true;
            }

            current = current.parent();
        }
        return false;
    }

    // Scheduled crawler to run all tech product categories
    @Scheduled(fixedRate = 43200000, initialDelay = 75000) // 12 hours, start after 75s
    public void scheduledCrawlAllTechProducts() {
        System.out.println(" Crawl tự động: Công nghệ/Điện tử");
        for (CategoryConfig config : configBySlug.values()) {
            try {
                crawl(config);
                Thread.sleep(500); // Small delay between categories
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
