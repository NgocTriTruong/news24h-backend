package com.news24h.news24hbackend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String password; // bcrypt

    // Chủ đề yêu thích: lưu dạng string đơn giản, ví dụ "the-thao,cong-nghe"
    private String favoriteCategories;
}

