package com.moviedates.backend.service;

import com.moviedates.backend.model.Session;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.SessionRepository;
import com.moviedates.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;
    private UserRepository userRepository;

    public Session createNewSession() {
        Session session = new Session();
        // Generates a short 6-character code
        session.setCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        return sessionRepository.save(session);
    }

    public Session getSessionByCode(String code) {
        return sessionRepository.findByCode(code).orElse(null);
    }

    public Session addUserToSession(String roomCode, Long userId) {
        Session session = sessionRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Polymorphism in action: 'participants' list accepts any User subclass
        if (!session.getParticipants().contains(user)) {
            session.getParticipants().add(user);
        }

        return sessionRepository.save(session);
    }
}