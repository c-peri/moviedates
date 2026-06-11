package com.moviedates.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.moviedates.backend.model.GuestUser;
import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
        // hashing password before saving
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public GuestUser createGuest(String nickname) {
        GuestUser guest = new GuestUser();

        if (nickname == null || nickname.trim().isEmpty()) {
            guest.setDisplayName("Guest_" + (System.currentTimeMillis() % 10000));
        } else {
            guest.setDisplayName(nickname);
        }

        return userRepository.save(guest);
    }

    public RegisteredUser login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!(user instanceof RegisteredUser)) {
            throw new RuntimeException("This email belongs to a non-registered account.");
        }

        RegisteredUser registeredUser = (RegisteredUser) user;

        if (!passwordEncoder.matches(password, registeredUser.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return registeredUser;
    }

    public RegisteredUser loginWithGoogle(String idToken, boolean[] isNew) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) throw new RuntimeException("Invalid Google token");

            GoogleIdToken.Payload payload = token.getPayload();
            String googleId = payload.getSubject();
            String email    = payload.getEmail();
            String name     = (String) payload.get("name");

            Optional<User> existing = userRepository.findByEmail(email);
            if (existing.isPresent()) {
                return (RegisteredUser) existing.get();
            }

            RegisteredUser user = new RegisteredUser();
            user.setEmail(email);
            user.setDisplayName(name);
            user.setGoogleId(googleId);
            user.setProvider(RegisteredUser.AuthProvider.GOOGLE);
            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Google auth failed: " + e.getMessage());
        }
    }
}