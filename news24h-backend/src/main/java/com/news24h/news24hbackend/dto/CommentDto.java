package com.news24h.news24hbackend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private String id;
    private String userName;
    private String content;
    private Instant createdAt;
}

