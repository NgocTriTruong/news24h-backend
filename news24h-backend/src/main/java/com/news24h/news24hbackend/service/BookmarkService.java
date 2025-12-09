package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.dto.BookmarkDto;
import com.news24h.news24hbackend.entity.*;
import com.news24h.news24hbackend.repository.BookmarkRepository;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import com.news24h.news24hbackend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepo;
    private final UserRepository userRepo;
    private final NewsArticleRepository newsRepo;

    public BookmarkService(BookmarkRepository bookmarkRepo,
                           UserRepository userRepo,
                           NewsArticleRepository newsRepo) {
        this.bookmarkRepo = bookmarkRepo;
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
    }

    private User currentUser(Authentication auth) {
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void addBookmark(String articleId, Authentication auth) {
        User user = currentUser(auth);
        NewsArticle article = newsRepo.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (bookmarkRepo.findByUserIdAndArticleId(user.getId(), articleId).isPresent()) {
            return;
        }

        Bookmark bm = Bookmark.builder()
                .user(user)
                .article(article)
                .createdAt(Instant.now())
                .build();
        bookmarkRepo.save(bm);
    }

    public void removeBookmark(String articleId, Authentication auth) {
        User user = currentUser(auth);
        bookmarkRepo.deleteByUserIdAndArticleId(user.getId(), articleId);
    }

    public List<BookmarkDto> getBookmarks(Authentication auth) {
        User user = currentUser(auth);
        return bookmarkRepo.findByUser(user)
                .stream()
                .map(bm -> BookmarkDto.builder()
                        .articleId(bm.getArticle().getId())
                        .title(bm.getArticle().getTitle())
                        .thumbnail(bm.getArticle().getThumbnail())
                        .category(bm.getArticle().getCategory())
                        .build())
                .collect(Collectors.toList());
    }
}

