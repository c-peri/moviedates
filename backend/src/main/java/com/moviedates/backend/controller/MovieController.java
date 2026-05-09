package com.moviedates.backend.controller;

import com.moviedates.backend.service.external.TMDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private TMDBService tmdbService;

    @GetMapping("/discover")
    public Map<String, Object> discoverMovies() {
        return tmdbService.getPopularMovies();
    }
}