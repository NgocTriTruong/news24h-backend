package com.news24h.news24hbackend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessage {
    private String role;
    private String text;
}
