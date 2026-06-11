package com.moviedates.backend.controller;

import com.moviedates.backend.dto.VoteRequest;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.VoteRepository;
import com.moviedates.backend.service.RecommendationService;
import com.moviedates.backend.service.SessionService;
import com.moviedates.backend.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ResponseEntity<?> handleSwipe(@RequestBody VoteRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.isFinished()) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "ALREADY_MATCHED");
            result.put("matchedMovieId", session.getMatchedMovieId());
            return ResponseEntity.ok(result);
        }

        if (session.getSwipingStartedAt() == null) {
            session.setSwipingStartedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }

        boolean isMatch = voteService.submitSwipe(
                request.getUserId(),
                session.getCode(),
                request.getMovieId(),
                request.isAccepted()
        );

        session.setTotalSwipes(session.getTotalSwipes() + 1);

        long userCount = session.getParticipants().size();
        if (voteRepository.isMovieAMatch(session.getId(), request.getMovieId(), userCount)) {
            return finishSession(session, request.getMovieId());
        }

        if (sessionService.isForceMatchCriteriaMet(session)) {
            Integer topMovie = voteRepository.findMostVotedMovie(session.getId());
            return finishSession(session, topMovie);
        }

        sessionRepository.save(session);
        return ResponseEntity.ok(Map.of("match", isMatch));
    }

    private ResponseEntity<?> finishSession(Session session, Integer movieId) {
        session.setMatchedMovieId(movieId);
        session.setFinished(true);
        sessionRepository.save(session);

        Map<String, Object> matchUpdate = Map.of(
                "status", "MATCHED",
                "movieId", movieId,
                "movieDetails", recommendationService.fetchSingleMovieDetails(movieId)
        );

        String destination = "/topic/session/" + session.getCode();
        messagingTemplate.convertAndSend(destination, matchUpdate, (Map<String, Object>) null);

        return ResponseEntity.ok(matchUpdate);
    }

}