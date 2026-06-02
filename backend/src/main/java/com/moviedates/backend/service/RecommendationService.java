package com.moviedates.backend.service;

import com.moviedates.backend.dto.MovieDTO;
import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.service.external.TMDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RecommendationService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.base.url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.img.url}")
    private String tmdbImgUrl;


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TMDBService tmdbService;



    public List<Integer> generateWeightedDeck(Session session) {
        List<Integer> finalDeck = new ArrayList<>();

        List<Integer> preferredGenres = session.getParticipants().stream()
                .filter(u -> u instanceof RegisteredUser)
                .flatMap(u -> ((RegisteredUser) u).getPreferredGenres().stream())
                .collect(Collectors.toList());

        // Genre recommendations
        if (!preferredGenres.isEmpty()) {
            List<Integer> prefMovies = extractIds(tmdbService.discoverMovies(preferredGenres));
            addWithLimit(finalDeck, prefMovies, 10); // 50%
        }

        // Popular
        List<Integer> popularMovies = extractIds(tmdbService.getPopularMovies());
        addWithLimit(finalDeck, popularMovies, 4); // 20%
        // trending
        List<Integer> trendingMovies = extractIds(tmdbService.getTrendingMovies());
        addWithLimit(finalDeck, trendingMovies, 4); // 20%
        //hidden gems
        List<Integer> hiddenGems = extractIds(tmdbService.discoverHiddenGems());
        addWithLimit(finalDeck, hiddenGems, 2); //10%

        //shuffle deck
        Collections.shuffle(finalDeck);
        return finalDeck;
    }

    private List<Integer> extractIds(Map<String, Object> response) {
        if (response == null || !response.containsKey("results")) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        return results.stream()
                .map(m -> (Integer) m.get("id"))
                .collect(Collectors.toList());
    }

    private void addWithLimit(List<Integer> mainList, List<Integer> source, int limit) {
        Set<Integer> existingIds = new HashSet<>(mainList);
        source.stream()
                .distinct()
                .filter(id -> !existingIds.contains(id))
                .limit(limit)
                .forEach(mainList::add);
    }


    @Cacheable(value = "movieDetails", key = "#movieId", unless = "#result == null")
    public MovieDTO fetchSingleMovieDetails(Integer movieId) {
        String url = tmdbBaseUrl + movieId + "?api_key=" + apiKey;
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                MovieDTO dto = new MovieDTO(
                        (Integer) response.get("id"),
                        (String) response.get("title"),
                        tmdbImgUrl + response.get("poster_path"),
                        (String) response.get("overview"),
                        (Double) response.get("vote_average"),
                        (String) response.get("release_date")
                );
                log.info("Successfully found from TMDB for ID: {}", movieId);
                return  dto;
            }
        } catch (Exception e) {
            log.error("TMDB call failed for ID " + movieId, e);
        }
        return null;
    }

}