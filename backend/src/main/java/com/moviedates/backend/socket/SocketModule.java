package com.moviedates.backend.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.moviedates.backend.dto.MovieDTO;
import com.moviedates.backend.model.Session;
import com.moviedates.backend.service.RecommendationService;
import com.moviedates.backend.service.SessionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SocketModule {

    private final SocketIOServer server;
    private final SessionService sessionService;
    private final RecommendationService recommendationService;

    public SocketModule(SocketIOServer server,
                        SessionService sessionService,
                        RecommendationService recommendationService) {
        this.server = server;
        this.sessionService = sessionService;
        this.recommendationService = recommendationService;

        this.server.addConnectListener(onConnected());
        this.server.addEventListener("join_room", String.class, onJoinRoom());
        this.server.addEventListener("request_deck", String.class, onRequestDeck());
        this.server.addEventListener("submit_swipe", SwipePayload.class, onSwipeSubmitted());
    }

    private ConnectListener onConnected() {
        return client -> log.info("Client paired to real-time swiping socket: {}", client.getSessionId());
    }

    private DataListener<String> onJoinRoom() {
        return (client, roomCode, ackSender) -> {
            client.joinRoom(roomCode);
            log.info("User joined room namespace: {}", roomCode);

            server.getRoomOperations(roomCode).sendEvent("user_joined", "A new friend joined!");
        };
    }

    private DataListener<String> onRequestDeck() {
        return (client, roomCode, ackSender) -> {
            Session session = sessionService.getSessionByCode(roomCode);

            if (session != null) {
                List<Integer> movieIds = session.getMovieDeck();

                if (movieIds == null || movieIds.isEmpty()) {
                    movieIds = recommendationService.generateWeightedDeck(session);
                }

                List<MovieDTO> detailedCards = new ArrayList<>();

                for (Integer id : movieIds) {
                    MovieDTO details = recommendationService.fetchSingleMovieDetails(id);
                    if (details != null) {
                        detailedCards.add(details);
                    }
                }

                client.sendEvent("receive_deck", detailedCards);
                log.info("Successfully pushed card deck with {} movies to room {}", detailedCards.size(), roomCode);
            } else {
                log.warn("Failed to generate deck: Room code {} was not found.", roomCode);
            }
        };
    }

    private DataListener<SwipePayload> onSwipeSubmitted() {
        return (client, payload, ackSender) -> {
            log.info("Swipe event: User {} marked {} on movie {} in room {}",
                    payload.getUserId(), payload.getDirection(), payload.getMovieId(), payload.getRoomCode());

            Session session = sessionService.getSessionByCode(payload.getRoomCode());
            if (session != null) {
                server.getRoomOperations(payload.getRoomCode()).sendEvent("peer_swiped", payload);

                if (sessionService.isForceMatchCriteriaMet(session)) {
                    log.info("No match yet for room: {}. System forcing match evaluations.", payload.getRoomCode());
                    server.getRoomOperations(payload.getRoomCode()).sendEvent("force_match_triggered", true);
                }
            }
        };
    }

    @Data
    static class SwipePayload {
        private String roomCode;
        private Long userId;
        private Integer movieId;
        private String direction;
    }
}