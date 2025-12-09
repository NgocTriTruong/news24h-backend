package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.Bookmark;
import com.news24h.news24hbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, String> {

    List<Bookmark> findByUser(User user);

    Optional<Bookmark> findByUserIdAndArticleId(String userId, String articleId);

    void deleteByUserIdAndArticleId(String userId, String articleId);
}

