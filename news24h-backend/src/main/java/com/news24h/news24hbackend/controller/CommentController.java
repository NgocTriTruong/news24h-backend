package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.CommentDto;
import com.news24h.news24hbackend.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{articleId}")
    public List<CommentDto> getComments(@PathVariable String articleId) {
        return commentService.getComments(articleId);
    }

    @PostMapping("/{articleId}")
    public CommentDto addComment(
            @PathVariable String articleId,
            @RequestBody String content,
            Authentication auth
    ) {
        return commentService.addComment(articleId, content, auth);
    }
}

