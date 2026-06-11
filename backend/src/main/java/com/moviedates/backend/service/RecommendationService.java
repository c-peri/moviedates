package com.moviedates.backend.service;

import com.moviedates.backend.dto.MovieDTO;
import com.moviedates.backend.dto.StreamingProviderDTO;
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
        return generateWeightedDeck(session, new ArrayList<>());
    }

    public List<Integer> generateWeightedDeck(Session session, List<Integer> excludeIds) {
        Set<Integer> excluded = new HashSet<>(excludeIds);
        List<Integer> finalDeck = new ArrayList<>();
        Random random = new Random();

        List<List<Integer>> allGenreLists = session.getParticipants().stream()
                .filter(u -> u instanceof RegisteredUser)
                .map(u -> ((RegisteredUser) u).getPreferredGenres())
                .collect(Collectors.toList());

        List<Integer> commonGenres = new ArrayList<>();
        List<Integer> anyGenres = new ArrayList<>();

        if (!allGenreLists.isEmpty()) {
            commonGenres = new ArrayList<>(allGenreLists.get(0));
            for (List<Integer> genres : allGenreLists) {
                commonGenres.retainAll(genres);
            }
            allGenreLists.forEach(anyGenres::addAll);
            anyGenres = anyGenres.stream().distinct().collect(Collectors.toList());
        }

        if (!commonGenres.isEmpty()) {
            for (int attempt = 0; finalDeck.size() < 8 && attempt < 5; attempt++) {
                int page = random.nextInt(8) + 1;
                List<Integer> ids = extractIds(tmdbService.discoverMovies(commonGenres, page))
                        .stream().filter(id -> !excluded.contains(id)).collect(Collectors.toList());
                addWithLimit(finalDeck, ids, 8 - finalDeck.size());
            }
        }

        if (!anyGenres.isEmpty() && finalDeck.size() < 12) {
            int page = random.nextInt(6) + 1;
            List<Integer> ids = extractIds(tmdbService.discoverMoviesByGenre(anyGenres, page))
                    .stream().filter(id -> !excluded.contains(id) && !finalDeck.contains(id))
                    .collect(Collectors.toList());
            addWithLimit(finalDeck, ids, 12 - finalDeck.size());
        }

        int popularPage = random.nextInt(15) + 1;
        List<Integer> popularMovies = extractIds(tmdbService.getPopularMovies(popularPage))
                .stream().filter(id -> !excluded.contains(id) && !finalDeck.contains(id))
                .collect(Collectors.toList());
        addWithLimit(finalDeck, popularMovies, 4);

        int gemsPage = random.nextInt(8) + 1;
        List<Integer> hiddenGems = extractIds(tmdbService.discoverHiddenGems(gemsPage))
                .stream().filter(id -> !excluded.contains(id) && !finalDeck.contains(id))
                .collect(Collectors.toList());
        addWithLimit(finalDeck, hiddenGems, 3);

        int topPage = random.nextInt(10) + 1;
        List<Integer> topRated = extractIds(tmdbService.getTopRatedMovies(topPage))
                .stream().filter(id -> !excluded.contains(id) && !finalDeck.contains(id))
                .collect(Collectors.toList());
        addWithLimit(finalDeck, topRated, 1);

        if (finalDeck.size() < 20) {
            int page = random.nextInt(10) + 1;
            List<Integer> topUp = extractIds(tmdbService.getPopularMovies(page))
                    .stream().filter(id -> !excluded.contains(id) && !finalDeck.contains(id))
                    .collect(Collectors.toList());
            addWithLimit(finalDeck, topUp, 20 - finalDeck.size());
        }

        Collections.shuffle(finalDeck);
        return finalDeck.stream().distinct().limit(20).collect(Collectors.toList());
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
        String url = tmdbBaseUrl + movieId + "?api_key=" + apiKey + "&language=en-US";
        try {
            Map<String, Object> data = restTemplate.getForObject(url, Map.class);
            if (data == null) return null;

            List<String> genreNames = new ArrayList<>();
            Object genresObj = data.get("genres");
            if (genresObj instanceof List) {
                for (Object g : (List<?>) genresObj) {
                    if (g instanceof Map) {
                        Object name = ((Map<?, ?>) g).get("name");
                        if (name != null) genreNames.add(name.toString());
                    }
                }
            }

            log.info("Successfully found from TMDB for ID: {}", movieId);

            return new MovieDTO(
                    (Integer) data.get("id"),
                    (String) data.get("title"),
                    tmdbImgUrl + data.get("poster_path"),
                    (String) data.get("overview"),
                    data.get("vote_average") != null ? ((Number) data.get("vote_average")).doubleValue() : 0.0,
                    (String) data.get("release_date"),
                    genreNames
            );

        } catch (Exception e) {
            log.error("TMDB call failed for ID: " + movieId, e);
            return null;
        }
    }

    @Cacheable(value = "streamingProviders", key = "#movieId", unless = "#result == null || #result.isEmpty()")
    public List<StreamingProviderDTO> fetchStreamingProviders(Integer movieId, String countryCode) {
        String url = tmdbBaseUrl + movieId + "/watch/providers?api_key=" + apiKey;
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return Collections.emptyList();

            Map<String, Object> results = (Map<String, Object>) response.get("results");
            if (results == null || !results.containsKey(countryCode)) return Collections.emptyList();

            Map<String, Object> countryData = (Map<String, Object>) results.get(countryCode);
            List<Map<String, Object>> flatrate = (List<Map<String, Object>>) countryData.get("flatrate");
            if (flatrate == null) return Collections.emptyList();

            return flatrate.stream()
                    .map(p -> new StreamingProviderDTO(
                            (String) p.get("provider_name"),
                            tmdbImgUrl + p.get("logo_path")
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch streaming providers for movie ID: " + movieId, e);
            return Collections.emptyList();
        }
    }

}