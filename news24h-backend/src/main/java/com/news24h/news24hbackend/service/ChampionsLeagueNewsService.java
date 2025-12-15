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
import java.util.stream.Collectors;

@Service
public class ChampionsLeagueNewsService {

    private final NewsArticleRepository newsRepository;
    private final ArticleCrawlerService crawlerService;

    public ChampionsLeagueNewsService(NewsArticleRepository newsRepository, ArticleCrawlerService crawlerService) {
        this.newsRepository = newsRepository;
        this.crawlerService = crawlerService;
    }

    /**
     * Crawl tin tức Cup C1 / Champions League
     */
    public List<NewsArticle> crawlChampionsLeagueNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            // Crawl từ trang Champions League của 24h
            String[] urls = {
                    "https://www.24h.com.vn/cup-c1-champions-league-c153.html"  // Trang Cup C1 chính thức
            };

            for (String url : urls) {
                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(15000)
                            .get();

                    // Lấy tất cả link bài viết - thử nhiều selector
                    Elements newsLinks = doc.select("h3 a, h2 a, .cate-24h-foot-home-list-item a, article a");

                    System.out.println("Tìm thấy " + newsLinks.size() + " links từ " + url);

                    for (Element link : newsLinks) {
                        try {
                            String title = link.text().trim();
                            String href = link.attr("abs:href");

                            // Loại bỏ các tin rõ ràng KHÔNG phải Cup C1
                            String titleLower = title.toLowerCase();
                            boolean isInvalidNews = titleLower.contains("v-league")
                                    || titleLower.contains("v.league")
                                    || titleLower.contains("việt nam")
                                    || titleLower.contains("ngoại hạng anh")
                                    || titleLower.contains("premier league")
                                    || titleLower.contains("laliga")
                                    || titleLower.contains("la liga")
                                    || titleLower.contains("serie a")
                                    || titleLower.contains("bundesliga")
                                    || titleLower.contains("ligue 1");

                            // Chỉ lấy tin CÓ từ khóa liên quan Cup C1
                            boolean hasC1Keyword = titleLower.contains("champions")
                                    || titleLower.contains("cup c1")
                                    || titleLower.contains("c1")
                                    || titleLower.contains("real")
                                    || titleLower.contains("barcelona") || titleLower.contains("barca")
                                    || titleLower.contains("bayern")
                                    || titleLower.contains("milan")
                                    || titleLower.contains("inter")
                                    || titleLower.contains("juventus") || titleLower.contains("juve")
                                    || titleLower.contains("psg") || titleLower.contains("paris")
                                    || titleLower.contains("liverpool")
                                    || titleLower.contains("manchester city") || titleLower.contains("man city")
                                    || titleLower.contains("chelsea")
                                    || titleLower.contains("arsenal")
                                    || titleLower.contains("dortmund")
                                    || titleLower.contains("atletico");

                            // Lấy tin có từ khóa Cup C1 VÀ không phải tin sai
                            if (!title.isEmpty() && title.length() > 10 &&
                                    href.contains("24h.com.vn") &&
                                    !href.contains("#") &&
                                    !href.contains("javascript") &&
                                    hasC1Keyword && !isInvalidNews) {

                                // Kiểm tra đã tồn tại chưa
                                if (newsRepository.findBySourceUrl(href).isEmpty()) {
                                    NewsArticle article = new NewsArticle();
                                    article.setTitle(title);
                                    article.setDescription("Tin tức về Champions League / Cup C1");
                                    article.setThumbnail("https://picsum.photos/800/400");
                                    article.setCategory("cup-c1");
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

                    if (articles.size() >= 15) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi crawl " + url + ": " + e.getMessage());
                }
            }

            System.out.println("Đã crawl " + articles.size() + " tin tức Cup C1");

        } catch (Exception e) {
            System.out.println("Lỗi crawl tin Cup C1: " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

    /**
     * Cleanup tin không phải Cup C1
     */
    public int cleanupNonChampionsLeagueNews() {
        List<NewsArticle> articles = newsRepository.findByCategoryOrderByPublishedAtDesc(
                "cup-c1",
                org.springframework.data.domain.PageRequest.of(0, 200)
        ).getContent();

        int count = 0;
        for (NewsArticle article : articles) {
            String title = article.getTitle();
            String titleLower = title.toLowerCase();
            String sourceUrl = article.getSourceUrl() != null ? article.getSourceUrl() : "";

            // Giữ tin có từ khóa Champions League
            boolean hasChampionsKeyword = titleLower.contains("champions league")
                    || titleLower.contains("cup c1")
                    || titleLower.contains("c1")
                    || titleLower.contains("uefa");

            // Xóa nếu không có từ khóa hoặc không từ 24h
            if (!hasChampionsKeyword || !sourceUrl.contains("24h.com.vn")) {
                System.out.println("Xóa tin không liên quan: " + article.getTitle());
                newsRepository.delete(article);
                count++;
            }
        }

        return count;
    }

    /**
     * Tự động crawl mỗi 3 giờ
     */
    @Scheduled(fixedRate = 10800000) // 3 giờ
    public void scheduledCrawl() {
        System.out.println("Bắt đầu crawl tin Cup C1 tự động...");
        crawlChampionsLeagueNews();
    }
}
