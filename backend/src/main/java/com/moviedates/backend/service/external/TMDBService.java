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

@Service
@Slf4j
public class TMDBService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final String BASE_URL = "https://api.themoviedb.org/3";
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String COMMON_FILTERS =
            "&language=en-US" +
                    "&with_original_language=en" +
                    "&include_adult=false" +
                    "&release_date.lte=" + java.time.LocalDate.now().minusMonths(3); // not in cinemas (released 3+ months ago)

    public Map<String, Object> getPopularMovies(int page) {
        String url = String.format("%s/discover/movie?api_key=%s&sort_by=popularity.desc&page=%d%s",
                BASE_URL, apiKey, page, COMMON_FILTERS);
        return safeGet(url);
    }

    public Map<String, Object> getTopRatedMovies(int page) {
        String url = String.format("%s/discover/movie?api_key=%s&sort_by=vote_average.desc&vote_count.gte=1000&page=%d%s",
                BASE_URL, apiKey, page, COMMON_FILTERS);
        return safeGet(url);
    }

    public Map<String, Object> discoverMovies(List<Integer> genreIds, int page) {
        String genres = genreIds.stream().map(Object::toString).collect(Collectors.joining(","));
        String url = String.format("%s/discover/movie?api_key=%s&with_genres=%s&sort_by=vote_average.desc&vote_count.gte=200&page=%d%s",
                BASE_URL, apiKey, genres, page, COMMON_FILTERS);
        return safeGet(url);
    }

    public Map<String, Object> discoverMoviesByGenre(List<Integer> genreIds, int page) {
        String genres = genreIds.stream().map(Object::toString).collect(Collectors.joining(","));
        String url = String.format("%s/discover/movie?api_key=%s&with_genres=%s&sort_by=popularity.desc&vote_count.gte=100&page=%d%s",
                BASE_URL, apiKey, genres, page, COMMON_FILTERS);
        return safeGet(url);
    }

    public Map<String, Object> discoverHiddenGems(int page) {
        String url = String.format("%s/discover/movie?api_key=%s&vote_average.gte=6.5&vote_count.gte=100&vote_count.lte=2000&sort_by=vote_average.desc&page=%d%s",
                BASE_URL, apiKey, page, COMMON_FILTERS);
        return safeGet(url);
    }

    public Map<String, Object> getGenres() {
        String url = String.format("%s/genre/movie/list?api_key=%s&language=en-US", BASE_URL, apiKey);
        return safeGet(url);
    }

    private Map<String, Object> safeGet(String url) {
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("TMDB call failed: {}", url, e);
            return Collections.emptyMap();
        }
    }
}