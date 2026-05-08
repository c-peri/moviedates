package com.moviedates.backend.service;

import com.corundumstudio.socketio.SocketIOServer; // Add this
import com.moviedates.backend.model.Session;
import com.moviedates.backend.model.Vote;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.VoteRepository;
import com.moviedates.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SocketIOServer socketServer; // Inject the server here

    public boolean submitSwipe(Long userId, String roomCode, Integer movieId, boolean accepted) {
        Session session = sessionRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Vote vote = new Vote();
        vote.setUser(userRepository.findById(userId).get());
        vote.setSession(session);
        vote.setMovieId(movieId);
        vote.setAccepted(accepted);

        voteRepository.save(vote);

        if (accepted) {
            boolean isMatch = checkForMatch(session, movieId);
            if (isMatch) {
                // BROADCAST: Tell everyone in the room!
                socketServer.getRoomOperations(roomCode).sendEvent("match_found", movieId);
            }
            return isMatch;
        }
        return false;
    }

    private boolean checkForMatch(Session session, Integer movieId) {
        long acceptCount = voteRepository.countAcceptances(session.getId(), movieId);
        long participantCount = session.getParticipants().size();
        return participantCount > 0 && acceptCount >= participantCount;
    }
}