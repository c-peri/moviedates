package com.moviedates.backend.controller;

import com.moviedates.backend.service.external.TMDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private TMDBService tmdbService;

    // GET /api/movies/popular
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularMovies() {
        Map<String, Object> popular = tmdbService.getPopularMovies();
        return ResponseEntity.ok(popular);
    }

    // GET /api/movies/trending
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingMovies() {
        Map<String, Object> trending = tmdbService.getTrendingMovies();
        return ResponseEntity.ok(trending);
    }

    // GET /api/movies/hidden-gems
    @GetMapping("/hidden-gems")
    public ResponseEntity<Map<String, Object>> getHiddenGems() {
        Map<String, Object> gems = tmdbService.discoverHiddenGems();
        return ResponseEntity.ok(gems);
    }

    // GET /api/movies/discover?genres=28,87
    @GetMapping("/discover")
    public ResponseEntity<Map<String, Object>> discoverMoviesByGenres(@RequestParam List<Integer> genres) {
        if (genres == null || genres.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Map<String, Object> filteredMovies = tmdbService.discoverMovies(genres);
        return ResponseEntity.ok(filteredMovies);
    }
}