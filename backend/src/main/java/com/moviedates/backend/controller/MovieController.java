package com.moviedates.backend.controller;

import com.moviedates.backend.service.external.TMDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private TMDBService tmdbService;

    @GetMapping
    public List<String> getMovies() {
        return List.of("The Matrix", "Inception", "Interstellar");
    }

    @GetMapping("/discover")
    public Map<String, Object> discoverMovies() {
        return tmdbService.getPopularMovies();
    }
}
