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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HealthNewsCrawlerService {

    private record CategoryConfig(String slug, String url, String description, int maxItems, long initialDelayMs) {}

    private final List<CategoryConfig> configs = List.of(
            new CategoryConfig("benh-dan-ong", "https://www.24h.com.vn/benh-dan-ong-c778.html", "Tin Bệnh đàn ông", 25, 72_000),
            new CategoryConfig("benh-phu-nu", "https://www.24h.com.vn/benh-phu-nu-c779.html", "Tin Bệnh phụ nữ", 25, 76_000),
            new CategoryConfig("dinh-duong", "https://www.24h.com.vn/dinh-duong-c780.html", "Tin Dinh dưỡng", 25, 80_000),
            new CategoryConfig("ung-thu", "https://www.24h.com.vn/ung-thu-c62e3457.html", "Tin Ung thư", 25, 84_000),
            new CategoryConfig("phat-minh-y-hoc", "https://www.24h.com.vn/phat-minh-y-hoc-c979.html", "Tin Phát minh y học", 25, 88_000),
            new CategoryConfig("tin-tuc-suc-khoe", "https://www.24h.com.vn/tin-tuc-suc-khoe-c683.html", "Tin Tin tức sức khỏe", 25, 92_000)
    );

    private final Map<String, CategoryConfig> configBySlug = new ConcurrentHashMap<>();
    private final NewsArticleRepository newsRepository;
    private final ArticleCrawlerService crawlerService;
    private final ThumbnailUpdateService thumbnailUpdateService;

    public HealthNewsCrawlerService(NewsArticleRepository newsRepository,
                                    ArticleCrawlerService crawlerService,
                                    ThumbnailUpdateService thumbnailUpdateService) {
        this.newsRepository = newsRepository;
        this.crawlerService = crawlerService;
        this.thumbnailUpdateService = thumbnailUpdateService;
        configs.forEach(c -> configBySlug.put(c.slug(), c));
    }

    public List<NewsArticle> crawlCategory(String slug) {
        CategoryConfig cfg = configBySlug.get(slug);
        if (cfg == null) {
            System.out.println("Không tìm thấy cấu hình cho slug: " + slug);
            return List.of();
        }
        return crawl(cfg);
    }

    private List<NewsArticle> crawl(CategoryConfig cfg) {
        List<NewsArticle> articles = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(cfg.url())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            Elements newsLinks = doc.select("h3 a, h2 a, .cate-24h-foot-home-list-item a, article a");
            System.out.println("Tìm thấy " + newsLinks.size() + " links từ " + cfg.url());

            for (Element link : newsLinks) {
                try {
                    String title = link.text().trim();
                    String href = link.attr("abs:href");

                    if (isAdOrSponsored(link)) {
                        continue;
                    }

                    if (!title.isEmpty() && title.length() > 8 &&
                            href.contains("24h.com.vn") &&
                            !href.contains("#") &&
                            !href.contains("javascript")) {

                        if (newsRepository.findBySourceUrl(href).isEmpty()) {
                            String thumbnail = null;
                            try {
                                Element parent = link.parent();
                                while (parent != null && thumbnail == null) {
                                    Element img = parent.selectFirst("img");
                                    if (img != null) {
                                        if (img.hasAttr("srcset") && !img.attr("srcset").isEmpty()) {
                                            String srcset = img.attr("srcset");
                                            String first = srcset.split(",")[0].trim().split("\\s+")[0];
                                            String resolved = img.baseUri().isEmpty() ? first : Jsoup.parse("<a href='" + first + "'>").selectFirst("a").attr("abs:href");
                                            thumbnail = normalizeThumbnail(resolved);
                                            if (isLogoImage(thumbnail)) {
                                                thumbnail = null;
                                            }
                                        }
                                        if (thumbnail == null && img.hasAttr("data-src")) {
                                            thumbnail = normalizeThumbnail(img.attr("abs:data-src"));
                                            if (isLogoImage(thumbnail)) {
                                                thumbnail = null;
                                            }
                                        } else if (thumbnail == null && img.hasAttr("data-original")) {
                                            thumbnail = normalizeThumbnail(img.attr("abs:data-original"));
                                            if (isLogoImage(thumbnail)) {
                                                thumbnail = null;
                                            }
                                        } else if (thumbnail == null && img.hasAttr("src")) {
                                            thumbnail = normalizeThumbnail(img.attr("abs:src"));
                                            if (isLogoImage(thumbnail)) {
                                                thumbnail = null;
                                            }
                                        }
                                    }
                                    if (thumbnail == null) {
                                        parent = parent.parent();
                                        if (parent != null && parent.tagName().equals("body")) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                // Ignore per-item errors
                            }

                            if (thumbnail == null) {
                                thumbnail = "https://picsum.photos/800/400";
                            }

                            NewsArticle article = new NewsArticle();
                            article.setTitle(title);
                            article.setDescription(cfg.description());
                            article.setThumbnail(thumbnail);
                            article.setCategory(cfg.slug());
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
                    // Skip lỗi từng item
                }

                if (articles.size() >= cfg.maxItems()) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi crawl " + cfg.slug() + ": " + e.getMessage());
        }

        if (!articles.isEmpty()) {
            System.out.println("Bắt đầu update thumbnails...");
            int updatedCount = thumbnailUpdateService.updatePlaceholderThumbnails();
            System.out.println("Đã update " + updatedCount + " thumbnails");
        }

        return articles;
    }

    private String normalizeThumbnail(String url) {
        if (url == null) {
            return null;
        }
        String cleaned = url.trim();
        String lower = cleaned.toLowerCase();

        if (cleaned.isEmpty()) {
            return null;
        }
        if (lower.startsWith("data:image") || lower.contains("r0lgodlhaqab") || lower.contains("1x1")) {
            return null;
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return null;
        }
        return cleaned;
    }

    private boolean isLogoImage(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.endsWith(".svg") || lower.contains("logo-24h") || lower.contains("/logo") || lower.contains("/favicon");
    }

    private boolean isAdOrSponsored(Element element) {
        if (element == null) {
            return false;
        }

        String classAttr = element.attr("class").toLowerCase();
        String idAttr = element.attr("id").toLowerCase();

        if (classAttr.contains("ad") || classAttr.contains("sponsored") || classAttr.contains("promo") ||
                idAttr.contains("ad") || idAttr.contains("sponsored") || idAttr.contains("promo")) {
            return true;
        }

        if (element.hasAttr("data-ad") || element.hasAttr("data-adslot")) {
            return true;
        }

        Element parent = element.parent();
        while (parent != null && !parent.tagName().equals("body")) {
            String parentClass = parent.attr("class").toLowerCase();
            if (parentClass.contains("ad") || parentClass.contains("sponsored") || parentClass.contains("promo")) {
                return true;
            }
            parent = parent.parent();
        }

        return false;
    }

    // Scheduler: crawl all health categories with small delay between them
    @Scheduled(fixedRate = 43200000, initialDelay = 72000)
    public void scheduledCrawlAll() {
        System.out.println("Tự động crawl health categories...");
        for (CategoryConfig cfg : configs) {
            try {
                Thread.sleep(500);
                crawl(cfg);
            } catch (Exception e) {
                System.out.println("Lỗi khi crawl " + cfg.slug() + ": " + e.getMessage());
            }
        }
    }
}
