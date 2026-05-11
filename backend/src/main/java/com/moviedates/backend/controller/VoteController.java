package com.moviedates.backend.controller;

import com.moviedates.backend.dto.VoteRequest;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.UserRepository;
import com.moviedates.backend.repository.VoteRepository;
import com.moviedates.backend.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.moviedates.backend.model.Vote;
import com.moviedates.backend.model.User;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/votes") // Changed from /api/swipes
public class VoteController {

    @Autowired
    private VoteService voteService;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserRepository userRepository;


    @PostMapping
    public ResponseEntity<?> handleSwipe(@RequestBody VoteRequest request) {
        // 1. Fetch the Session
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.isFinished()) {
            return ResponseEntity.ok(Map.of("status", "finished", "match", session.getMatchedMovieId()));
        }

        // 1. Initialize timer on the very first swipe of the session
        if (session.getSwipingStartedAt() == null) {
            session.setSwipingStartedAt(LocalDateTime.now());
        }

        // 2. Save the Vote
        Vote vote = new Vote();
        vote.setUser(userRepository.getReferenceById(request.getUserId()));
        vote.setSession(session);
        vote.setMovieId(request.getMovieId());
        vote.setAccepted(request.isAccepted());
        voteRepository.save(vote);

        // 3. Increment Swipe Count
        session.setTotalSwipes(session.getTotalSwipes() + 1);

        // 4. Check for Natural Match (Everyone liked it)
        long userCount = session.getParticipants().size();
        if (voteRepository.isMovieAMatch(session.getId(), request.getMovieId(), userCount)) {
            return finishSession(session, request.getMovieId());
        }

        // 5. Check for Forced Match (Swipe Limit Hit)
        if (session.getTotalSwipes() >= 50) {
            Integer topMovie = voteRepository.findMostVotedMovie(session.getId());
            return finishSession(session, topMovie);
        }

        sessionRepository.save(session);
        return ResponseEntity.ok(Map.of("match", false));
    }

    private ResponseEntity<?> finishSession(Session session, Integer movieId) {
        session.setMatchedMovieId(movieId);
        session.setFinished(true);
        sessionRepository.save(session);
        return ResponseEntity.ok(Map.of("match", true, "movieId", movieId, "forced", session.getTotalSwipes() >= 50));
    }
}