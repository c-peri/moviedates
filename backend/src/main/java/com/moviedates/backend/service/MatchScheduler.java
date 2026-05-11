package com.moviedates.backend.service;

import com.moviedates.backend.model.Session;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.VoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class MatchScheduler {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private VoteRepository voteRepository;

    // Runs every 10 seconds (10000ms)
    @Scheduled(fixedRate = 10000)
    @Transactional // Ensures database updates are saved correctly
    public void checkTimeLimits() {
        // Find sessions where swiping started more than 5 minutes ago
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);

        List<Session> timedOutSessions = sessionRepository.findTimedOutSessions(cutoff);

        if (!timedOutSessions.isEmpty()) {
            log.info("Found {} timed out sessions. Forcing matches...", timedOutSessions.size());
        }

        for (Session session : timedOutSessions) {
            // Use the "Most Voted" query we built earlier
            Integer topMovie = voteRepository.findMostVotedMovie(session.getId());

            if (topMovie != null) {
                log.info("Session {}: Forced match found -> Movie ID {}", session.getId(), topMovie);
                session.setMatchedMovieId(topMovie);
                session.setFinished(true);
                sessionRepository.save(session);
            } else {
                // If NO ONE voted for anything, we just close the session
                log.warn("Session {}: Timed out but no votes were cast.", session.getId());
                session.setFinished(true);
                sessionRepository.save(session);
            }
        }
    }
}