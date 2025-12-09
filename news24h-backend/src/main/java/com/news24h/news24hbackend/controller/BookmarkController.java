package com.news24h.news24hbackend.controller;

import com.news24h.news24hbackend.dto.BookmarkDto;
import com.news24h.news24hbackend.service.BookmarkService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public List<BookmarkDto> getBookmarks(Authentication auth) {
        return bookmarkService.getBookmarks(auth);
    }

    @PostMapping("/{articleId}")
    public void addBookmark(@PathVariable String articleId, Authentication auth) {
        bookmarkService.addBookmark(articleId, auth);
    }

    @DeleteMapping("/{articleId}")
    public void removeBookmark(@PathVariable String articleId, Authentication auth) {
        bookmarkService.removeBookmark(articleId, auth);
    }
}

