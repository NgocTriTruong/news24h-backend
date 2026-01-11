package com.news24h.news24hbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordDefinitionResponse {
    private boolean success;
    private String word;
    private String definition;
    private String partOfSpeech; // danh từ, động từ, tính từ
    private String example;
    private String message;
}
