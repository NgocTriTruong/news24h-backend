package com.news24h.news24hbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordDefinitionRequest {
    private String word;
    private String language; // "vi" hoặc "en"
}
