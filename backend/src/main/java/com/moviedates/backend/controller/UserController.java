package com.moviedates.backend.controller;

import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.UserRepository;
import com.moviedates.backend.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired private UserRepository userRepository;

    @PostMapping("/guest")
    public User createGuest(@RequestParam String name) {
        return userService.createGuest(name);
    }

    @PutMapping("/{id}/genres")
    public ResponseEntity<?> updateGenres(@PathVariable Long id, @RequestBody List<Integer> genreIds) {
        RegisteredUser user = (RegisteredUser) userRepository.findById(id).orElseThrow();
        user.setPreferredGenres(genreIds);
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping(value = "/{id}/solo-deck", produces = "application/json")
    public ResponseEntity<String> getSoloSwipeDeck(@PathVariable Long id) {
        String tmdbJsonDeck = userService.getSoloSwipeDeckFromTmdb(id);
        return ResponseEntity.ok(tmdbJsonDeck);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<RegisteredUser> updatePreferences(
            @PathVariable Long id,
            @RequestBody PreferencesRequest request) {
        RegisteredUser updatedUser = userService.updatePreferences(id, request.getGenres(), request.getMovies());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<User> updateProfile(
            @PathVariable Long id,
            @RequestBody ProfileUpdateRequest request) {
        User updatedUser = userService.updateProfile(id, request.getDisplayName(), request.getPhotoUrl());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUserById(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Account deleted successfully.");
    }

    @Data
    static class PreferencesRequest {
        private List<Integer> genres;
        private List<Integer> movies;
    }

    @Data
    static class ProfileUpdateRequest {
        private String displayName;
        private String photoUrl;
    }
}