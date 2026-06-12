package com.moviedates.backend.service;

import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.UserRepository;
import com.moviedates.backend.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VoteRepository voteRepository;

    public Session createNewSession() {
        Session session = new Session();
        session.setCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        return sessionRepository.save(session);
    }

    @Autowired
    private RecommendationService recommendationService;

    public Session startSession(String roomCode) {
        Session session = sessionRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Integer> deck = recommendationService.generateWeightedDeck(session);

        session.setMovieDeck(deck);
        session.setSwipingStartedAt(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    public Session getSessionByCode(String code) {
        return sessionRepository.findByCode(code).orElse(null);
    }


    public Session joinSession(String code, Long userId) {
        Session session = sessionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!session.getParticipants().contains(user)) {
            session.getParticipants().add(user);
        }

        return sessionRepository.save(session);
    }

    public boolean isForceMatchCriteriaMet(Session session) {
        boolean fiveMinsPassed = session.getSwipingStartedAt() != null &&
                session.getSwipingStartedAt().isBefore(LocalDateTime.now().minusMinutes(5));

        boolean fiftyTotalSwipes = session.getTotalSwipes() >= 50;

        boolean everyoneMetMinimum = false;

        Long participantsWhoFinished = voteRepository.countUsersMinimumSwipes(session.getId(), 15L);

        if (participantsWhoFinished != null) {
            everyoneMetMinimum = participantsWhoFinished >= session.getParticipants().size();
        }

        return fiveMinsPassed && fiftyTotalSwipes && everyoneMetMinimum;
    }


}