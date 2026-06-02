package com.moviedates.backend.controller;

import com.moviedates.backend.dto.MovieDTO;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.service.RecommendationService;
import com.moviedates.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<Integer> newIds = recommendationService.generateWeightedDeck(session);

        session.getMovieDeck().addAll(newIds);
        sessionRepository.save(session);

        List<MovieDTO> cardStack = newIds.stream()
                .map(recommendationService::fetchSingleMovieDetails)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cardStack);
    }

}