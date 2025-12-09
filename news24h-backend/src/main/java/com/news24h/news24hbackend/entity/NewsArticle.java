package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "news_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String thumbnail;

    private String category;

    private String sourceUrl; // link bài báo gốc

    private Instant publishedAt;

    private long viewCount;

    private boolean featured;
}

