package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.GoldPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoldPriceRepository extends JpaRepository<GoldPrice, String> {

    // Lấy giá vàng mới nhất theo loại vàng và công ty
    GoldPrice findFirstByGoldTypeAndCompanyOrderByCrawledAtDesc(String goldType, String company);

    // Lấy tất cả giá vàng mới nhất theo công ty
    List<GoldPrice> findByCompanyOrderByCrawledAtDesc(String company);

    // Lấy tất cả giá vàng
    List<GoldPrice> findAllByOrderByCrawledAtDesc();
}
