package com.moviedates.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Hashes passwords with salt automatically
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabled for mobile/stateless APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/movies/**").permitAll() // Allow auth and movie browsing
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}