package com.news24h.news24hbackend.service;

import com.news24h.news24hbackend.dto.CommentDto;
import com.news24h.news24hbackend.entity.Comment;
import com.news24h.news24hbackend.entity.NewsArticle;
import com.news24h.news24hbackend.entity.User;
import com.news24h.news24hbackend.repository.CommentRepository;
import com.news24h.news24hbackend.repository.NewsArticleRepository;
import com.news24h.news24hbackend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepo;
    private final NewsArticleRepository newsRepo;
    private final UserRepository userRepo;

    public CommentService(CommentRepository commentRepo,
                          NewsArticleRepository newsRepo,
                          UserRepository userRepo) {
        this.commentRepo = commentRepo;
        this.newsRepo = newsRepo;
        this.userRepo = userRepo;
    }

    private User currentUser(Authentication auth) {
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<CommentDto> getComments(String articleId) {
        return commentRepo.findByArticleIdOrderByCreatedAtAsc(articleId)
                .stream()
                .map(c -> CommentDto.builder()
                        .id(c.getId())
                        .userName(c.getUser().getName())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public CommentDto addComment(String articleId, String content, Authentication auth) {
        User user = currentUser(auth);
        NewsArticle article = newsRepo.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        Comment c = Comment.builder()
                .article(article)
                .user(user)
                .content(content)
                .createdAt(Instant.now())
                .build();

        commentRepo.save(c);

        return CommentDto.builder()
                .id(c.getId())
                .userName(user.getName())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

