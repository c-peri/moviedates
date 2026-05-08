package com.moviedates.backend.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import org.springframework.stereotype.Component;

@Component
public class SocketModule {

    private final SocketIOServer server;

    public SocketModule(SocketIOServer server) {
        this.server = server;
        this.server.addConnectListener(onConnected());
        this.server.addEventListener("join_room", String.class, onJoinRoom());
    }

    private ConnectListener onConnected() {
        return client -> System.out.println("Client connected: " + client.getSessionId());
    }

    private DataListener<String> onJoinRoom() {
        return (client, roomCode, ackSender) -> {
            client.joinRoom(roomCode);
            System.out.println("User joined room: " + roomCode);

            // Notify everyone in the room that someone new joined
            server.getRoomOperations(roomCode).sendEvent("user_joined", "A new friend joined!");
        };
    }
}