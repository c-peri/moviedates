package com.moviedates.backend.service;

import com.moviedates.backend.model.Session;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.VoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class MatchScheduler {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired SessionService sessionService;

    @Scheduled(fixedRate = 10000)
    public void checkTimeLimits() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);

        List<Session> timedOutSessions = sessionRepository.findTimedOutSessions(cutoff);

        if (!timedOutSessions.isEmpty()) {
            log.info("Found {} timed out sessions. Forcing matches...", timedOutSessions.size());
        }

        for (Session session : timedOutSessions) {
            try {
                if (sessionService.isForceMatchCriteriaMet(session)) {
                    Integer topMovie = voteRepository.findMostVotedMovie(session.getId());
                    if (topMovie != null) {
                        session.setMatchedMovieId(topMovie);
                        session.setFinished(true);
                        sessionRepository.save(session);
                        log.info("Session Code {} forced match to Movie ID {}", session.getCode(), topMovie);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process timeout match sequence for Session ID: " + session.getId(), e);
            }
        }
    }
}