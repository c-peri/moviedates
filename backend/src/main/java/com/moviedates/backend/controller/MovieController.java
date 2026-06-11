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

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularMovies() {
        return ResponseEntity.ok(tmdbService.getPopularMovies(1));
    }

    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingMovies() {
        return ResponseEntity.ok(tmdbService.getTopRatedMovies(1));
    }

    @GetMapping("/hidden-gems")
    public ResponseEntity<Map<String, Object>> getHiddenGems() {
        return ResponseEntity.ok(tmdbService.discoverHiddenGems(1));
    }

    @GetMapping("/discover")
    public ResponseEntity<Map<String, Object>> discoverMoviesByGenres(@RequestParam List<Integer> genres) {
        if (genres == null || genres.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tmdbService.discoverMovies(genres, 1));
    }

    @GetMapping("/genres")
    public ResponseEntity<Map<String, Object>> getGenres() {
        return ResponseEntity.ok(tmdbService.getGenres());
    }
}