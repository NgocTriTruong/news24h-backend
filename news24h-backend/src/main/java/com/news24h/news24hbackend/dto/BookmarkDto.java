package com.news24h.news24hbackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkDto {
    private String articleId;
    private String title;
    private String thumbnail;
    private String category;
}

