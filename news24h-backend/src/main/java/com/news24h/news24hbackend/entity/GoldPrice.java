package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gold_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoldPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String goldType; // Loại vàng: "SJC", "9999", "9999 nhẫn trơn", v.v.

    @Column(nullable = false)
    private Double buyPrice; // Giá mua vào

    @Column(nullable = false)
    private Double sellPrice; // Giá bán ra

    @Column(nullable = false)
    private String company; // Công ty: "SJC", "PNJ", "Doji", v.v.

    @Column(nullable = false)
    private LocalDateTime crawledAt; // Thời điểm crawl

    @Column(nullable = false)
    private LocalDateTime updatedAt; // Thời điểm cập nhật
}
