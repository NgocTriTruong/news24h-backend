package com.news24h.news24hbackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummaryRequest {
    private String articleId;
    private String content; // fallback nếu FE gửi thẳng nội dung
}

