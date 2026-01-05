package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    // GOOGLE / FACEBOOK / ZALO
    private String provider;

    // id bên provider
    private String providerId;

    private String avatarUrl;

    @Column(nullable = true)
    private String password;

    private Instant createdAt;

    // Chủ đề yêu thích: lưu dạng string đơn giản, ví dụ "the-thao,cong-nghe"
    private String favoriteCategories;
}

