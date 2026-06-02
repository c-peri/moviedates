package com.moviedates.backend.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TMDBService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final String BASE_URL = "https://api.themoviedb.org/3";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getPopularMovies() {
        String url = String.format("%s/movie/popular?api_key=%s&language=en-US&page=1", BASE_URL, apiKey);
        return restTemplate.getForObject(url, Map.class);
    }


    public Map<String, Object> discoverMovies(List<Integer> genreIds) {
        String genres = genreIds.stream().map(Object::toString).collect(Collectors.joining(","));
        String url = String.format("%s/discover/movie?api_key=%s&with_genres=%s&sort_by=popularity.desc", BASE_URL, apiKey, genres);
        return restTemplate.getForObject(url, Map.class);
    }

    public Map<String, Object> getTrendingMovies() {
        String url = String.format("%s/trending/movie/week?api_key=%s", BASE_URL, apiKey);
        return restTemplate.getForObject(url, Map.class);
    }
    public Map<String, Object> discoverHiddenGems() {
        int randomPage = new Random().nextInt(5) + 1;
        String url = BASE_URL + "/discover/movie"
                + "?api_key=" + apiKey
                + "&vote_average.gte=6.0"
                + "&vote_count.gte=100"
                + "&vote_count.lte=1500"
                + "&sort_by=vote_average.desc"
                + "&include_adult=false"
                + "&page=" + randomPage;

        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch hidden gems from TMDB", e);
            return Collections.emptyMap();
        }
    }

}