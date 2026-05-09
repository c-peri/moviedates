package com.moviedates.backend.service;

import com.moviedates.backend.model.GuestUser;
import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisteredUser register(String email, String password, String name) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        RegisteredUser user = new RegisteredUser();
        user.setEmail(email);
        user.setDisplayName(name);
        // HASH the password before saving!
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public GuestUser createGuest(String nickname) {
        GuestUser guest = new GuestUser();

        // If the user left the field empty, we can still provide a fallback
        if (nickname == null || nickname.trim().isEmpty()) {
            guest.setDisplayName("Guest_" + (System.currentTimeMillis() % 10000));
        } else {
            guest.setDisplayName(nickname);
        }

        // guest.setEmail(null) is implicit
        return userRepository.save(guest);
    }

    public RegisteredUser login(String email, String password) {
        // 1. Find the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if the user is actually a "RegisteredUser" (Polymorphism check)
        if (!(user instanceof RegisteredUser)) {
            throw new RuntimeException("This email belongs to a non-registered account.");
        }

        RegisteredUser registeredUser = (RegisteredUser) user;

        // 3. Verify password
        if (!passwordEncoder.matches(password, registeredUser.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return registeredUser;
    }
}