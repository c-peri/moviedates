package com.moviedates.backend.config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SocketRunner {

    @Autowired
    private SocketIOServer server;

    @PostConstruct
    public void start() {
        server.start();
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }
}