package com.moviedates.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moviedates.backend.model.GuestUser;
import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CacheManager cacheManager;

    public User createGuest(String name) {
        GuestUser guest = new GuestUser();
        guest.setDisplayName(name);
        return userRepository.save(guest);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    public RegisteredUser updatePreferences(Long userId, List<Integer> genres, List<Integer> movies) {
        RegisteredUser user = (RegisteredUser) userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!(user instanceof RegisteredUser registeredUser)) {
            throw new IllegalArgumentException("Preferences can only be updated for registered user accounts.");
        }
        user.setPreferredGenres(genres);
        user.setFavouriteMovies(movies);

        return userRepository.save(user);
    }

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getSoloSwipeDeckFromTmdb(Long userId) {
        try {
            RegisteredUser user = (RegisteredUser) userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<Integer> alreadyLikedIds = user.getFavouriteMovies();

            HttpClient client = HttpClient.newHttpClient();

            ObjectNode finalResponseNode = objectMapper.createObjectNode();
            ArrayNode deckResults = objectMapper.createArrayNode();

            int currentPage = 1;
            int targetSize = 20;

            while (deckResults.size() < targetSize && currentPage <= 10) {
                String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + tmdbApiKey + "&page=" + currentPage;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonNode rootNode = objectMapper.readTree(response.body());
                ArrayNode resultsArray = (ArrayNode) rootNode.get("results");

                if (resultsArray != null) {
                    for (JsonNode movieNode : resultsArray) {
                        if (deckResults.size() >= targetSize) {
                            break;
                        }

                        int tmdbId = movieNode.get("id").asInt();
                        if (!alreadyLikedIds.contains(tmdbId)) {
                            deckResults.add(movieNode);
                        }
                    }
                }
                currentPage++;
            }

            finalResponseNode.set("results", deckResults);
            finalResponseNode.put("total_results", deckResults.size());

            return objectMapper.writeValueAsString(finalResponseNode);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to generate 20-movie deck\"}";
        }
    }

    @Transactional
    public boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            cacheManager.getCache("users").evict(id);

            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public User updateProfile(Long userId, String name, String photoUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDisplayName(name);
        user.setPhotoUrl(photoUrl);

        return userRepository.save(user);
    }
}