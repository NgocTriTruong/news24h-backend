package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.NewsArticle;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, String> {

    Page<NewsArticle> findByCategoryOrderByPublishedAtDesc(
            String category, Pageable pageable);

    Page<NewsArticle> findByTitleContainingIgnoreCase(
            String keyword, Pageable pageable);

    List<NewsArticle> findTop5ByFeaturedTrueOrderByPublishedAtDesc();

    List<NewsArticle> findTop10ByOrderByPublishedAtDesc();

    List<NewsArticle> findTop5ByCategoryAndIdNotOrderByPublishedAtDesc(
            String category, String excludeId);

    Optional<NewsArticle> findBySourceUrl(String sourceUrl);
}

