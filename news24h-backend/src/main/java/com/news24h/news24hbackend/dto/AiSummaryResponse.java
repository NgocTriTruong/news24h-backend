package com.news24h.news24hbackend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSummaryResponse {
    private List<String> bullets;
}

