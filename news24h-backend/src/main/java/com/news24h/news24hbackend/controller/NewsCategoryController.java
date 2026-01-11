package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/api/news-categories")
@CrossOrigin(origins = "*")
public class NewsCategoryController {

    private final SocietyLifeNewsService societyLifeNewsService;
    private final TrafficAccidentNewsService trafficAccidentNewsService;
    private final RealEstateNewsService realEstateNewsService;
    private final StartupNewsService startupNewsService;
    private final ShowbizNewsService showbizNewsService;
    private final SportsNewsService sportsNewsService;
    private final HealthNewsCrawlerService healthNewsCrawlerService;
    private final TechProductsCrawlerService techProductsCrawlerService;

    public NewsCategoryController(SocietyLifeNewsService societyLifeNewsService,
                                  TrafficAccidentNewsService trafficAccidentNewsService,
                                  RealEstateNewsService realEstateNewsService,
                                  StartupNewsService startupNewsService,
                                  ShowbizNewsService showbizNewsService,
                                  SportsNewsService sportsNewsService,
                                  HealthNewsCrawlerService healthNewsCrawlerService,
                                  TechProductsCrawlerService techProductsCrawlerService) {
        this.societyLifeNewsService = societyLifeNewsService;
        this.trafficAccidentNewsService = trafficAccidentNewsService;
        this.realEstateNewsService = realEstateNewsService;
        this.startupNewsService = startupNewsService;
        this.showbizNewsService = showbizNewsService;
        this.sportsNewsService = sportsNewsService;
        this.healthNewsCrawlerService = healthNewsCrawlerService;
        this.techProductsCrawlerService = techProductsCrawlerService;
    }

    // Tắt auto-crawl khi startup để tăng tốc độ khởi động
    // @PostConstruct
    // public void initializeCrawling() {
    //     System.out.println("=== Bắt đầu crawl tin tức khi khởi động ===");
    //     crawlAllNewsCategories();
    // }

    @PostMapping("/doi-song-dan-sinh/crawl")
    public ResponseEntity<String> crawlSocietyLife() {
        List<NewsArticle> articles = societyLifeNewsService.crawlSocietyLifeNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Đời sống - Dân sinh");
    }

    @PostMapping("/tai-nan-giao-thong/crawl")
    public ResponseEntity<String> crawlTrafficAccident() {
        List<NewsArticle> articles = trafficAccidentNewsService.crawlTrafficAccidentNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Tai nạn giao thông");
    }


    @PostMapping("/bat-dong-san/crawl")
    public ResponseEntity<String> crawlRealEstate() {
        List<NewsArticle> articles = realEstateNewsService.crawlRealEstateNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bất động sản");
    }

    @PostMapping("/khoi-nghiep/crawl")
    public ResponseEntity<String> crawlStartup() {
        List<NewsArticle> articles = startupNewsService.crawlStartupNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Khởi nghiệp");
    }

    @PostMapping("/doi-song-showbiz/crawl")
    public ResponseEntity<String> crawlShowbiz() {
        List<NewsArticle> articles = showbizNewsService.crawlShowbizNews();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Đời sống showbiz");
    }

    @PostMapping("/pickleball/crawl")
    public ResponseEntity<String> crawlPickleball() {
        List<NewsArticle> articles = sportsNewsService.crawlPickleball();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Pickleball");
    }

    @PostMapping("/bong-chuyen/crawl")
    public ResponseEntity<String> crawlVolleyball() {
        List<NewsArticle> articles = sportsNewsService.crawlVolleyball();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bóng chuyền");
    }

    @PostMapping("/bong-ro-nba-vba/crawl")
    public ResponseEntity<String> crawlBasketball() {
        List<NewsArticle> articles = sportsNewsService.crawlBasketball();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bóng rổ");
    }

    @PostMapping("/tennis/crawl")
    public ResponseEntity<String> crawlTennis() {
        List<NewsArticle> articles = sportsNewsService.crawlTennis();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Tennis");
    }

    @PostMapping("/cac-mon-the-thao-khac/crawl")
    public ResponseEntity<String> crawlOtherSports() {
        List<NewsArticle> articles = sportsNewsService.crawlOtherSports();
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Thể thao khác");
    }

    @PostMapping("/benh-dan-ong/crawl")
    public ResponseEntity<String> crawlBenhDanOng() {
        List<NewsArticle> articles = healthNewsCrawlerService.crawlCategory("benh-dan-ong");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bệnh đàn ông");
    }

    @PostMapping("/benh-phu-nu/crawl")
    public ResponseEntity<String> crawlBenhPhuNu() {
        List<NewsArticle> articles = healthNewsCrawlerService.crawlCategory("benh-phu-nu");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Bệnh phụ nữ");
    }

    @PostMapping("/dinh-duong/crawl")
    public ResponseEntity<String> crawlDinhDuong() {
        List<NewsArticle> articles = healthNewsCrawlerService.crawlCategory("dinh-duong");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Dinh dưỡng");
    }

    @PostMapping("/ung-thu/crawl")
    public ResponseEntity<String> crawlUngThu() {
        List<NewsArticle> articles = healthNewsCrawlerService.crawlCategory("ung-thu");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Ung thư");
    }

    @PostMapping("/phat-minh-y-hoc/crawl")
    public ResponseEntity<String> crawlPhatMinhYHoc() {
        List<NewsArticle> articles = healthNewsCrawlerService.crawlCategory("phat-minh-y-hoc");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Phát minh y học");
    }

    @PostMapping("/tin-tuc-suc-khoe/crawl")
    public ResponseEntity<String> crawlTinTucSucKhoe() {
        List<NewsArticle> articles = healthNewsCrawlerService.crawlCategory("tin-tuc-suc-khoe");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Tin tức sức khỏe");
    }

    // DANH MỤC ƯU TIÊN TEST GIAO DIỆN
    @PostMapping("/diem-nong/crawl")
    public ResponseEntity<String> crawlDiemNong() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("diem-nong");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Điểm nóng");
    }

    @PostMapping("/quan-su/crawl")
    public ResponseEntity<String> crawlQuanSu() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("quan-su");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Quân sự");
    }

    @PostMapping("/theo-dong-lich-su/crawl")
    public ResponseEntity<String> crawlTheoDongLichSu() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("theo-dong-lich-su");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Theo dòng lịch sử");
    }

    @PostMapping("/dien-thoai/crawl")
    public ResponseEntity<String> crawlDienThoai() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("dien-thoai");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Điện thoại");
    }

    @PostMapping("/laptop-gia-re/crawl")
    public ResponseEntity<String> crawlLaptopGiaRe() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("laptop-gia-re");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Laptop giá rẻ");
    }

    @PostMapping("/may-tinh-de-ban/crawl")
    public ResponseEntity<String> crawlMayTinhDeBan() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("may-tinh-de-ban");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Máy tính để bàn");
    }

    @PostMapping("/may-tinh-bang/crawl")
    public ResponseEntity<String> crawlMayTinhBang() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("may-tinh-bang");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Máy tính bảng");
    }

    @PostMapping("/tin-tuc-cong-nghe/crawl")
    public ResponseEntity<String> crawlTinTucCongNghe() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("tin-tuc-cong-nghe");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Tin tức công nghệ");
    }

    @PostMapping("/cac-san-pham-khac/crawl")
    public ResponseEntity<String> crawlCacSanPhamKhac() {
        List<NewsArticle> articles = techProductsCrawlerService.crawlCategory("cac-san-pham-khac");
        return ResponseEntity.ok("Đã crawl " + articles.size() + " tin Các sản phẩm khác");
    }


    @PostMapping("/crawl-all")
    public ResponseEntity<String> crawlAllNewsCategories() {
        int total = 0;
        StringBuilder result = new StringBuilder();

        // ƯU TIÊN CRAWL DANH MỤC TEST GIAO DIỆN TRƯỚC
        List<NewsArticle> diemNong = techProductsCrawlerService.crawlCategory("diem-nong");
        total += diemNong.size();
        result.append("Điểm nóng: ").append(diemNong.size()).append(" tin\n");

        List<NewsArticle> quanSu = techProductsCrawlerService.crawlCategory("quan-su");
        total += quanSu.size();
        result.append("Quân sự: ").append(quanSu.size()).append(" tin\n");

        List<NewsArticle> theoDongLichSu = techProductsCrawlerService.crawlCategory("theo-dong-lich-su");
        total += theoDongLichSu.size();
        result.append("Theo dòng lịch sử: ").append(theoDongLichSu.size()).append(" tin\n");

        // CRAWL CÔNG NGHỆ
        List<NewsArticle> dienThoai = techProductsCrawlerService.crawlCategory("dien-thoai");
        total += dienThoai.size();
        result.append("Điện thoại: ").append(dienThoai.size()).append(" tin\n");

        List<NewsArticle> laptopGiaRe = techProductsCrawlerService.crawlCategory("laptop-gia-re");
        total += laptopGiaRe.size();
        result.append("Laptop giá rẻ: ").append(laptopGiaRe.size()).append(" tin\n");

        List<NewsArticle> mayTinhDeBan = techProductsCrawlerService.crawlCategory("may-tinh-de-ban");
        total += mayTinhDeBan.size();
        result.append("Máy tính để bàn: ").append(mayTinhDeBan.size()).append(" tin\n");

        List<NewsArticle> mayTinhBang = techProductsCrawlerService.crawlCategory("may-tinh-bang");
        total += mayTinhBang.size();
        result.append("Máy tính bảng: ").append(mayTinhBang.size()).append(" tin\n");

        List<NewsArticle> tinTucCongNghe = techProductsCrawlerService.crawlCategory("tin-tuc-cong-nghe");
        total += tinTucCongNghe.size();
        result.append("Tin tức công nghệ: ").append(tinTucCongNghe.size()).append(" tin\n");

        List<NewsArticle> cacSanPhamKhac = techProductsCrawlerService.crawlCategory("cac-san-pham-khac");
        total += cacSanPhamKhac.size();
        result.append("Các sản phẩm khác: ").append(cacSanPhamKhac.size()).append(" tin\n");

        List<NewsArticle> societyLife = societyLifeNewsService.crawlSocietyLifeNews();
        total += societyLife.size();
        result.append("Đời sống - Dân sinh: ").append(societyLife.size()).append(" tin\n");

        List<NewsArticle> trafficAccident = trafficAccidentNewsService.crawlTrafficAccidentNews();
        total += trafficAccident.size();
        result.append("Tai nạn giao thông: ").append(trafficAccident.size()).append(" tin\n");



        List<NewsArticle> realEstate = realEstateNewsService.crawlRealEstateNews();
        total += realEstate.size();
        result.append("Bất động sản: ").append(realEstate.size()).append(" tin\n");

        List<NewsArticle> startup = startupNewsService.crawlStartupNews();
        total += startup.size();
        result.append("Khởi nghiệp: ").append(startup.size()).append(" tin\n");

        List<NewsArticle> showbiz = showbizNewsService.crawlShowbizNews();
        total += showbiz.size();
        result.append("Đời sống showbiz: ").append(showbiz.size()).append(" tin\n");

        List<NewsArticle> pickleball = sportsNewsService.crawlPickleball();
        total += pickleball.size();
        result.append("Pickleball: ").append(pickleball.size()).append(" tin\n");

        List<NewsArticle> volleyball = sportsNewsService.crawlVolleyball();
        total += volleyball.size();
        result.append("Bóng chuyền: ").append(volleyball.size()).append(" tin\n");

        List<NewsArticle> basketball = sportsNewsService.crawlBasketball();
        total += basketball.size();
        result.append("Bóng rổ: ").append(basketball.size()).append(" tin\n");

        List<NewsArticle> tennis = sportsNewsService.crawlTennis();
        total += tennis.size();
        result.append("Tennis: ").append(tennis.size()).append(" tin\n");

        List<NewsArticle> otherSports = sportsNewsService.crawlOtherSports();
        total += otherSports.size();
        result.append("Thể thao khác: ").append(otherSports.size()).append(" tin\n");

        List<NewsArticle> benhDanOng = healthNewsCrawlerService.crawlCategory("benh-dan-ong");
        total += benhDanOng.size();
        result.append("Bệnh đàn ông: ").append(benhDanOng.size()).append(" tin\n");

        List<NewsArticle> benhPhuNu = healthNewsCrawlerService.crawlCategory("benh-phu-nu");
        total += benhPhuNu.size();
        result.append("Bệnh phụ nữ: ").append(benhPhuNu.size()).append(" tin\n");

        List<NewsArticle> dinhDuong = healthNewsCrawlerService.crawlCategory("dinh-duong");
        total += dinhDuong.size();
        result.append("Dinh dưỡng: ").append(dinhDuong.size()).append(" tin\n");

        List<NewsArticle> ungThu = healthNewsCrawlerService.crawlCategory("ung-thu");
        total += ungThu.size();
        result.append("Ung thư: ").append(ungThu.size()).append(" tin\n");

        List<NewsArticle> phatMinhYHoc = healthNewsCrawlerService.crawlCategory("phat-minh-y-hoc");
        total += phatMinhYHoc.size();
        result.append("Phát minh y học: ").append(phatMinhYHoc.size()).append(" tin\n");

        List<NewsArticle> tinTucSucKhoe = healthNewsCrawlerService.crawlCategory("tin-tuc-suc-khoe");
        total += tinTucSucKhoe.size();
        result.append("Tin tức sức khỏe: ").append(tinTucSucKhoe.size()).append(" tin\n");

        result.append("\nTổng: ").append(total).append(" tin");
        return ResponseEntity.ok(result.toString());
    }
}
