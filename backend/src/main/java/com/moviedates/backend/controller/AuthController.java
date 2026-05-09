package com.moviedates.backend.controller;

import com.moviedates.backend.model.GuestUser;
import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.service.AuthService;
import com.moviedates.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        RegisteredUser user = authService.register(
                request.get("email"),
                request.get("password"),
                request.get("name")
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        RegisteredUser user = authService.login(
                request.get("email"),
                request.get("password")
        );
        // In a real app, we would return a JWT token here
        return ResponseEntity.ok(user);
    }

    @PostMapping("/guest")
    public ResponseEntity<?> registerGuest(@RequestBody Map<String, String> request) {
        GuestUser guest = authService.createGuest(request.get("nickname"));

        // Use a unique string like "GUEST_123" so the JWT is valid
        String token = jwtService.generateToken("GUEST_" + guest.getId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", guest
        ));
    }
}