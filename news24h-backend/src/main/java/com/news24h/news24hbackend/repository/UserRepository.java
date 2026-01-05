package com.news24h.news24hbackend.repository;

import com.news24h.news24hbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}

