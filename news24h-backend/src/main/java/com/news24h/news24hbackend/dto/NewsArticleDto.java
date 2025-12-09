package com.news24h.news24hbackend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleDto {
    private String id;
    private String title;
    private String description;
    private String content;
    private String thumbnail;
    private String category;
    private String sourceUrl;
    private Instant publishedAt;
    private long viewCount;
    private boolean featured;
}

