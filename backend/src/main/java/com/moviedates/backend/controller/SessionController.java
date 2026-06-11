package com.moviedates.backend.controller;

import com.moviedates.backend.dto.MovieDTO;
import com.moviedates.backend.dto.StreamingProviderDTO;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.service.RecommendationService;
import com.moviedates.backend.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionRepository sessionRepository;

    @PostMapping("/create")
    public ResponseEntity<Session> createRoom() {
        return ResponseEntity.ok(sessionService.createNewSession());
    }

    @PostMapping("/start/{code}")
    public ResponseEntity<Session> startSession(@PathVariable String code) {
        Session session = sessionService.startSession(code);
        if (session != null) {
            return ResponseEntity.ok(session);
        }
        return ResponseEntity.status(404).body(null);
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<Session> joinRoom(@PathVariable String code, @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Session session = sessionService.joinSession(code, userId);
        if (session != null && session.isActive()) {
            return ResponseEntity.ok(session);
        }
        return ResponseEntity.status(404).body(null);
    }

    @GetMapping("/{code}/deck")
    public ResponseEntity<List<MovieDTO>> getMoreMovies(@PathVariable String code) {
        Session session = sessionService.getSessionByCode(code);
        if (session == null) return ResponseEntity.status(404).build();

        List<Integer> deckIds = session.getMovieDeck();

        if (deckIds == null || deckIds.isEmpty()) {
            List<Integer> newDeck = recommendationService.generateWeightedDeck(session, session.getSeenMovies());
            session.setMovieDeck(new ArrayList<>(newDeck));
            session.getSeenMovies().addAll(newDeck);
            sessionRepository.save(session);
            deckIds = newDeck;
        }

        List<MovieDTO> cardStack = deckIds.stream()
                .map(recommendationService::fetchSingleMovieDetails)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        session.setMovieDeck(new ArrayList<>());
        sessionRepository.save(session);

        return ResponseEntity.ok(cardStack);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Session> getSession(@PathVariable String code) {
        Session session = sessionService.getSessionByCode(code);
        if (session == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{code}/match/providers")
    public ResponseEntity<List<StreamingProviderDTO>> getMatchedMovieProviders(
            @PathVariable String code,
            @RequestParam(defaultValue = "GR") String country) {

        Session session = sessionService.getSessionByCode(code);

        if (session == null || session.getMatchedMovieId() == 0) {
            return ResponseEntity.status(404).build();
        }

        List<StreamingProviderDTO> providers =
                recommendationService.fetchStreamingProviders((int) session.getMatchedMovieId(), country);

        return ResponseEntity.ok(providers);
    }

}