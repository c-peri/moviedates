package com.moviedates.backend.repository;

import com.moviedates.backend.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    Optional<User> findByEmail(String email);
    }