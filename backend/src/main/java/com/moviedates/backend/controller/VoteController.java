package com.moviedates.backend.controller;

import com.moviedates.backend.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes") // Changed from /api/swipes
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitVote(@RequestParam Long userId,
                                             @RequestParam String roomCode,
                                             @RequestParam Integer movieId,
                                             @RequestParam boolean accepted) {

        // The service logic remains the same, but the naming is now consistent
        boolean isMatch = voteService.submitSwipe(userId, roomCode, movieId, accepted);

        if (isMatch) {
            return ResponseEntity.ok("MATCH_FOUND");
        }
        return ResponseEntity.ok("VOTE_RECORDED");
    }
}