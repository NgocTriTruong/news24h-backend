package com.news24h.news24hbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoldPriceDto {
    private String id;
    private String goldType;
    private Double buyPrice;
    private Double sellPrice;
    private String company;
    private LocalDateTime crawledAt;
    private LocalDateTime updatedAt;
}
