package com.moviedates.backend.service.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class TMDBService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final String BASE_URL = "https://api.themoviedb.org/3";
    private final RestTemplate restTemplate = new RestTemplate();

    // Fetches popular movies to start the swiping session
    public Map<String, Object> getPopularMovies() {
        String url = String.format("%s/movie/popular?api_key=%s&language=en-US&page=1", BASE_URL, apiKey);
        return restTemplate.getForObject(url, Map.class);
    }

    // Fetches specific movie details (for the Match screen)
    public Map<String, Object> getMovieDetails(Integer movieId) {
        String url = String.format("%s/movie/%d?api_key=%s&language=en-US", BASE_URL, movieId, apiKey);
        return restTemplate.getForObject(url, Map.class);
    }
}