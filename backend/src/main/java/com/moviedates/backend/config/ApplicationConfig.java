package com.moviedates.backend.config;

import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository repository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (username.startsWith("GUEST_")) {
                // Extract the ID from "GUEST_123"
                Long id = Long.parseLong(username.replace("GUEST_", ""));
                return repository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("Guest not found"));
            }

            // Otherwise, treat it as a standard email lookup
            return repository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        };
    }
}